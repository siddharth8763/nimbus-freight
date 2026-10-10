package com.nimbusfreight.entity;

import com.nimbusfreight.domain.AlertStatus;
import com.nimbusfreight.domain.AlertType;
import com.nimbusfreight.domain.ResolutionReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false, length = 32)
    private String truckId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "peak_temp_c", precision = 6, scale = 2)
    private BigDecimal peakTempC;

    @Column(name = "min_temp_c", precision = 6, scale = 2)
    private BigDecimal minTempC;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_reason", length = 16)
    private ResolutionReason resolutionReason;

    @Column(nullable = false, length = 1000)
    private String message;

    protected Alert() {
    }

    public Alert(String truckId, AlertType type, Instant startedAt, BigDecimal extremeTemp, String message) {
        this.truckId = truckId;
        this.type = type;
        this.status = AlertStatus.OPEN;
        this.startedAt = startedAt;
        this.message = message;
        if (type == AlertType.HIGH_TEMP) {
            this.peakTempC = extremeTemp;
        } else {
            this.minTempC = extremeTemp;
        }
    }

    public Long getId() {
        return id;
    }

    public String getTruckId() {
        return truckId;
    }

    public AlertType getType() {
        return type;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public BigDecimal getPeakTempC() {
        return peakTempC;
    }

    public BigDecimal getMinTempC() {
        return minTempC;
    }

    public ResolutionReason getResolutionReason() {
        return resolutionReason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void resolve(Instant resolvedAt, ResolutionReason reason) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
        this.resolutionReason = reason;
    }

    public void noteExtreme(BigDecimal temp) {
        if (type == AlertType.HIGH_TEMP) {
            if (peakTempC == null || temp.compareTo(peakTempC) > 0) {
                peakTempC = temp;
            }
        } else if (minTempC == null || temp.compareTo(minTempC) < 0) {
            minTempC = temp;
        }
    }
}
