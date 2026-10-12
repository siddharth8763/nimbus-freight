package com.nimbusfreight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "sensor_readings",
        uniqueConstraints = @UniqueConstraint(name = "uq_truck_time", columnNames = {"truck_id", "recorded_at"})
)
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false, length = 32)
    private String truckId;

    @Column(name = "temperature_c", nullable = false, precision = 6, scale = 2)
    private BigDecimal temperatureC;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected SensorReading() {
    }

    public SensorReading(String truckId, BigDecimal temperatureC, Instant recordedAt) {
        this.truckId = truckId;
        this.temperatureC = temperatureC;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTruckId() {
        return truckId;
    }

    public BigDecimal getTemperatureC() {
        return temperatureC;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
