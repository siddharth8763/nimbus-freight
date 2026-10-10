package com.nimbusfreight.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReadingRequest(String truckId, BigDecimal temperatureC, Instant recordedAt) {
}
