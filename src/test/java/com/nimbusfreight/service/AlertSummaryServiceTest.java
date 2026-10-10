package com.nimbusfreight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.config.OpenAiProperties;
import com.nimbusfreight.domain.AlertType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertSummaryServiceTest {

    @Test
    void usesTemplateWhenApiKeyMissing() {
        OpenAiProperties properties = new OpenAiProperties();
        AlertSummaryService service = new AlertSummaryService(properties, new AlertPolicy(), new ObjectMapper());

        String warm = service.summarize("Aurora", AlertType.HIGH_TEMP, new BigDecimal("9.2"), 15);
        String freeze = service.summarize("Cirrus", AlertType.LOW_TEMP, new BigDecimal("0.2"), 15);

        assertTrue(warm.contains("Aurora"));
        assertTrue(warm.contains("9.2"));
        assertTrue(freeze.contains("Cirrus"));
        assertTrue(freeze.contains("0.2"));
    }
}
