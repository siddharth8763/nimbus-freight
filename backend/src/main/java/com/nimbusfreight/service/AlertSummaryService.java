package com.nimbusfreight.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.config.OpenAiProperties;
import com.nimbusfreight.domain.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class AlertSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AlertSummaryService.class);

    private final OpenAiProperties openAi;
    private final AlertPolicy policy;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public AlertSummaryService(OpenAiProperties openAi, AlertPolicy policy, ObjectMapper objectMapper) {
        this.openAi = openAi;
        this.policy = policy;
        this.objectMapper = objectMapper;
    }

    public String summarize(String truckName, AlertType type, BigDecimal extremeTemp, int sustainMinutes) {
        String fallback = template(truckName, type, extremeTemp, sustainMinutes);
        if (!openAi.enabled()) {
            return fallback;
        }
        try {
            String generated = requestSummary(truckName, type, extremeTemp, sustainMinutes);
            if (generated == null || generated.isBlank()) {
                return fallback;
            }
            return generated.length() > 500 ? generated.substring(0, 500).trim() : generated.trim();
        } catch (Exception ex) {
            log.warn("LLM alert summary failed, using template: {}", ex.getMessage());
            return fallback;
        }
    }

    public String template(String truckName, AlertType type, BigDecimal extremeTemp, int sustainMinutes) {
        if (type == AlertType.HIGH_TEMP) {
            return "Truck " + truckName + " has been above " + policy.getThresholdHighC()
                    + "°C for at least " + sustainMinutes + " minutes (peak " + extremeTemp + "°C).";
        }
        return "Truck " + truckName + " has been below " + policy.getThresholdFreezeC()
                + "°C for at least " + sustainMinutes + " minutes (low " + extremeTemp + "°C).";
    }

    private String requestSummary(String truckName, AlertType type, BigDecimal extremeTemp, int sustainMinutes) throws Exception {
        String facts = template(truckName, type, extremeTemp, sustainMinutes);
        Map<String, Object> body = Map.of(
                "model", openAi.getModel(),
                "max_tokens", 80,
                "messages", java.util.List.of(
                        Map.of("role", "system", "content",
                                "You write one short plain-English operations alert for a refrigerated truck. No markdown, no preamble."),
                        Map.of("role", "user", "content",
                                "Rewrite this alert in one sentence for a dispatcher. Keep the facts: " + facts)
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openAi.getBaseUrl().replaceAll("/$", "") + "/chat/completions"))
                .timeout(Duration.ofSeconds(4))
                .header("Authorization", "Bearer " + openAi.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenAI HTTP " + response.statusCode());
        }
        JsonNode content = objectMapper.readTree(response.body()).path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? null : content.asText();
    }
}
