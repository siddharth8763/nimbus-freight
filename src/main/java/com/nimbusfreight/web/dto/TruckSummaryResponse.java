package com.nimbusfreight.web.dto;

import com.nimbusfreight.domain.CargoType;
import com.nimbusfreight.domain.TruckStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TruckSummaryResponse(
        String id,
        String name,
        CargoType cargoType,
        BigDecimal latestTempC,
        Instant latestRecordedAt,
        TruckStatus status,
        AlertResponse openAlert
) {
}
