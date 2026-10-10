package com.nimbusfreight.domain;

import java.math.BigDecimal;
import java.time.Instant;

public interface AlertSink {

    long open(AlertType type, Instant startedAt, BigDecimal extremeTemp);

    void resolve(long alertId, Instant resolvedAt, ResolutionReason reason);

    void updateExtreme(long alertId, BigDecimal temp, AlertType type);
}
