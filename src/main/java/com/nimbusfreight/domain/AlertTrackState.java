package com.nimbusfreight.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mutable per-truck timers used by the alert engine. Open-alert ids stay set across sensor gaps;
 * episode timers do not.
 */
public class AlertTrackState {

    private Instant lastProcessedAt;
    private Instant warmSince;
    private Instant warmResolveSince;
    private BigDecimal warmPeak;
    private Instant freezeSince;
    private Instant freezeResolveSince;
    private BigDecimal freezeMin;
    private Long openHighAlertId;
    private Long openLowAlertId;

    public void clearEpisodeTimers() {
        warmSince = null;
        warmResolveSince = null;
        warmPeak = null;
        freezeSince = null;
        freezeResolveSince = null;
        freezeMin = null;
    }

    public Instant getLastProcessedAt() {
        return lastProcessedAt;
    }

    public void setLastProcessedAt(Instant lastProcessedAt) {
        this.lastProcessedAt = lastProcessedAt;
    }

    public Instant getWarmSince() {
        return warmSince;
    }

    public void setWarmSince(Instant warmSince) {
        this.warmSince = warmSince;
    }

    public Instant getWarmResolveSince() {
        return warmResolveSince;
    }

    public void setWarmResolveSince(Instant warmResolveSince) {
        this.warmResolveSince = warmResolveSince;
    }

    public BigDecimal getWarmPeak() {
        return warmPeak;
    }

    public void setWarmPeak(BigDecimal warmPeak) {
        this.warmPeak = warmPeak;
    }

    public Instant getFreezeSince() {
        return freezeSince;
    }

    public void setFreezeSince(Instant freezeSince) {
        this.freezeSince = freezeSince;
    }

    public Instant getFreezeResolveSince() {
        return freezeResolveSince;
    }

    public void setFreezeResolveSince(Instant freezeResolveSince) {
        this.freezeResolveSince = freezeResolveSince;
    }

    public BigDecimal getFreezeMin() {
        return freezeMin;
    }

    public void setFreezeMin(BigDecimal freezeMin) {
        this.freezeMin = freezeMin;
    }

    public Long getOpenHighAlertId() {
        return openHighAlertId;
    }

    public void setOpenHighAlertId(Long openHighAlertId) {
        this.openHighAlertId = openHighAlertId;
    }

    public Long getOpenLowAlertId() {
        return openLowAlertId;
    }

    public void setOpenLowAlertId(Long openLowAlertId) {
        this.openLowAlertId = openLowAlertId;
    }
}
