package com.nimbusfreight.web.dto;

import com.nimbusfreight.domain.AlertStatus;
import com.nimbusfreight.domain.AlertType;
import com.nimbusfreight.domain.ResolutionReason;
import com.nimbusfreight.entity.Alert;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertResponse(
        Long id,
        String truckId,
        String truckName,
        AlertType type,
        AlertStatus status,
        Instant startedAt,
        Instant resolvedAt,
        BigDecimal peakTempC,
        BigDecimal minTempC,
        ResolutionReason resolutionReason,
        String message
) {
    public static AlertResponse from(Alert alert, String truckName) {
        return new AlertResponse(
                alert.getId(),
                alert.getTruckId(),
                truckName,
                alert.getType(),
                alert.getStatus(),
                alert.getStartedAt(),
                alert.getResolvedAt(),
                alert.getPeakTempC(),
                alert.getMinTempC(),
                alert.getResolutionReason(),
                alert.getMessage()
        );
    }
}
