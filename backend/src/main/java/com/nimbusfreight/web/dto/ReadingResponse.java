package com.nimbusfreight.web.dto;

import com.nimbusfreight.entity.SensorReading;

import java.math.BigDecimal;
import java.time.Instant;

public record ReadingResponse(Long id, String truckId, BigDecimal temperatureC, Instant recordedAt) {
    public static ReadingResponse from(SensorReading reading) {
        return new ReadingResponse(reading.getId(), reading.getTruckId(), reading.getTemperatureC(), reading.getRecordedAt());
    }
}
