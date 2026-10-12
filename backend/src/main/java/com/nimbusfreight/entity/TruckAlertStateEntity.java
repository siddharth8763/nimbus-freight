package com.nimbusfreight.entity;

import com.nimbusfreight.domain.AlertTrackState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "truck_alert_state")
public class TruckAlertStateEntity {

    @Id
    @Column(name = "truck_id", length = 32)
    private String truckId;

    @Column(name = "last_processed_at")
    private Instant lastProcessedAt;

    @Column(name = "warm_since")
    private Instant warmSince;

    @Column(name = "warm_resolve_since")
    private Instant warmResolveSince;

    @Column(name = "warm_peak_c", precision = 6, scale = 2)
    private BigDecimal warmPeak;

    @Column(name = "freeze_since")
    private Instant freezeSince;

    @Column(name = "freeze_resolve_since")
    private Instant freezeResolveSince;

    @Column(name = "freeze_min_c", precision = 6, scale = 2)
    private BigDecimal freezeMin;

    @Column(name = "open_high_alert_id")
    private Long openHighAlertId;

    @Column(name = "open_low_alert_id")
    private Long openLowAlertId;

    protected TruckAlertStateEntity() {
    }

    public TruckAlertStateEntity(String truckId) {
        this.truckId = truckId;
    }

    public AlertTrackState toState() {
        AlertTrackState state = new AlertTrackState();
        state.setLastProcessedAt(lastProcessedAt);
        state.setWarmSince(warmSince);
        state.setWarmResolveSince(warmResolveSince);
        state.setWarmPeak(warmPeak);
        state.setFreezeSince(freezeSince);
        state.setFreezeResolveSince(freezeResolveSince);
        state.setFreezeMin(freezeMin);
        state.setOpenHighAlertId(openHighAlertId);
        state.setOpenLowAlertId(openLowAlertId);
        return state;
    }

    public void apply(AlertTrackState state) {
        this.lastProcessedAt = state.getLastProcessedAt();
        this.warmSince = state.getWarmSince();
        this.warmResolveSince = state.getWarmResolveSince();
        this.warmPeak = state.getWarmPeak();
        this.freezeSince = state.getFreezeSince();
        this.freezeResolveSince = state.getFreezeResolveSince();
        this.freezeMin = state.getFreezeMin();
        this.openHighAlertId = state.getOpenHighAlertId();
        this.openLowAlertId = state.getOpenLowAlertId();
    }

    public String getTruckId() {
        return truckId;
    }

    public Long getOpenHighAlertId() {
        return openHighAlertId;
    }

    public Long getOpenLowAlertId() {
        return openLowAlertId;
    }
}
