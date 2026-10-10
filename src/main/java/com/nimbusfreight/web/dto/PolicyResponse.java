package com.nimbusfreight.web.dto;

import com.nimbusfreight.config.AlertPolicy;

import java.math.BigDecimal;

public record PolicyResponse(
        BigDecimal thresholdHighC,
        BigDecimal thresholdHighResolveC,
        BigDecimal thresholdFreezeC,
        BigDecimal thresholdFreezeResolveC,
        int sustainDurationMinutes,
        int resolveDurationMinutes,
        int gapResetMinutes
) {
    public static PolicyResponse from(AlertPolicy policy) {
        return new PolicyResponse(
                policy.getThresholdHighC(),
                policy.getThresholdHighResolveC(),
                policy.getThresholdFreezeC(),
                policy.getThresholdFreezeResolveC(),
                policy.getSustainDurationMinutes(),
                policy.getResolveDurationMinutes(),
                policy.getGapResetMinutes()
        );
    }
}
