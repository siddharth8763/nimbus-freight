package com.nimbusfreight.engine;

import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.domain.AlertSink;
import com.nimbusfreight.domain.AlertTrackState;
import com.nimbusfreight.domain.AlertType;
import com.nimbusfreight.domain.ResolutionReason;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Component
public class TemperatureAlertEngine {

    public void evaluate(AlertTrackState state, Instant recordedAt, BigDecimal temp, AlertPolicy policy, AlertSink sink) {
        Instant previous = state.getLastProcessedAt();
        if (previous != null && Duration.between(previous, recordedAt).compareTo(Duration.ofMinutes(policy.getGapResetMinutes())) > 0) {
            // Unknown cargo state during a long outage: do not carry a partial episode across the gap.
            // An alert that is already open stays open until later readings resolve or supersede it.
            state.clearEpisodeTimers();
        }

        boolean warmViolation = temp.compareTo(policy.getThresholdHighC()) > 0;
        boolean freezeViolation = temp.compareTo(policy.getThresholdFreezeC()) < 0;

        trackWarm(state, recordedAt, temp, policy, sink, warmViolation, freezeViolation);
        trackFreeze(state, recordedAt, temp, policy, sink, warmViolation, freezeViolation);
        state.setLastProcessedAt(recordedAt);
    }

    private void trackWarm(
            AlertTrackState state,
            Instant recordedAt,
            BigDecimal temp,
            AlertPolicy policy,
            AlertSink sink,
            boolean warmViolation,
            boolean freezeViolation
    ) {
        boolean safeToResolve = temp.compareTo(policy.getThresholdHighResolveC()) <= 0;

        if (warmViolation) {
            state.setFreezeSince(null);
            state.setFreezeResolveSince(null);
            state.setFreezeMin(null);
            if (state.getWarmSince() == null) {
                state.setWarmSince(recordedAt);
                state.setWarmPeak(temp);
            } else if (state.getWarmPeak() == null || temp.compareTo(state.getWarmPeak()) > 0) {
                state.setWarmPeak(temp);
            }
            state.setWarmResolveSince(null);
            if (state.getOpenHighAlertId() != null) {
                sink.updateExtreme(state.getOpenHighAlertId(), temp, AlertType.HIGH_TEMP);
            } else if (sustained(state.getWarmSince(), recordedAt, policy.getSustainDurationMinutes())) {
                supersede(state, recordedAt, sink, AlertType.LOW_TEMP);
                long id = sink.open(AlertType.HIGH_TEMP, state.getWarmSince(), state.getWarmPeak());
                state.setOpenHighAlertId(id);
            }
            return;
        }

        if (state.getOpenHighAlertId() == null) {
            state.setWarmSince(null);
            state.setWarmResolveSince(null);
            state.setWarmPeak(null);
            return;
        }

        if (freezeViolation) {
            // Opposite violation is in progress. Keep the warm alert until freeze sustains and supersedes it,
            // instead of resolving "back to normal" while the box is actually freezing.
            state.setWarmResolveSince(null);
            return;
        }

        if (safeToResolve) {
            if (state.getWarmResolveSince() == null) {
                state.setWarmResolveSince(recordedAt);
            }
            if (sustained(state.getWarmResolveSince(), recordedAt, policy.getResolveDurationMinutes())) {
                sink.resolve(state.getOpenHighAlertId(), recordedAt, ResolutionReason.NORMAL);
                state.setOpenHighAlertId(null);
                state.setWarmSince(null);
                state.setWarmResolveSince(null);
                state.setWarmPeak(null);
            }
            return;
        }

        state.setWarmResolveSince(null);
    }

    private void trackFreeze(
            AlertTrackState state,
            Instant recordedAt,
            BigDecimal temp,
            AlertPolicy policy,
            AlertSink sink,
            boolean warmViolation,
            boolean freezeViolation
    ) {
        boolean safeToResolve = temp.compareTo(policy.getThresholdFreezeResolveC()) >= 0;

        if (freezeViolation) {
            state.setWarmSince(null);
            state.setWarmResolveSince(null);
            state.setWarmPeak(null);
            if (state.getFreezeSince() == null) {
                state.setFreezeSince(recordedAt);
                state.setFreezeMin(temp);
            } else if (state.getFreezeMin() == null || temp.compareTo(state.getFreezeMin()) < 0) {
                state.setFreezeMin(temp);
            }
            state.setFreezeResolveSince(null);
            if (state.getOpenLowAlertId() != null) {
                sink.updateExtreme(state.getOpenLowAlertId(), temp, AlertType.LOW_TEMP);
            } else if (sustained(state.getFreezeSince(), recordedAt, policy.getSustainDurationMinutes())) {
                supersede(state, recordedAt, sink, AlertType.HIGH_TEMP);
                long id = sink.open(AlertType.LOW_TEMP, state.getFreezeSince(), state.getFreezeMin());
                state.setOpenLowAlertId(id);
            }
            return;
        }

        if (warmViolation && state.getOpenLowAlertId() != null) {
            state.setFreezeResolveSince(null);
            return;
        }

        if (state.getOpenLowAlertId() == null) {
            state.setFreezeSince(null);
            state.setFreezeResolveSince(null);
            state.setFreezeMin(null);
            return;
        }

        if (safeToResolve) {
            if (state.getFreezeResolveSince() == null) {
                state.setFreezeResolveSince(recordedAt);
            }
            if (sustained(state.getFreezeResolveSince(), recordedAt, policy.getResolveDurationMinutes())) {
                sink.resolve(state.getOpenLowAlertId(), recordedAt, ResolutionReason.NORMAL);
                state.setOpenLowAlertId(null);
                state.setFreezeSince(null);
                state.setFreezeResolveSince(null);
                state.setFreezeMin(null);
            }
            return;
        }

        state.setFreezeResolveSince(null);
    }

    private void supersede(AlertTrackState state, Instant recordedAt, AlertSink sink, AlertType closing) {
        if (closing == AlertType.LOW_TEMP && state.getOpenLowAlertId() != null) {
            sink.resolve(state.getOpenLowAlertId(), recordedAt, ResolutionReason.SUPERSEDED);
            state.setOpenLowAlertId(null);
            state.setFreezeSince(null);
            state.setFreezeResolveSince(null);
            state.setFreezeMin(null);
        }
        if (closing == AlertType.HIGH_TEMP && state.getOpenHighAlertId() != null) {
            sink.resolve(state.getOpenHighAlertId(), recordedAt, ResolutionReason.SUPERSEDED);
            state.setOpenHighAlertId(null);
            state.setWarmSince(null);
            state.setWarmResolveSince(null);
            state.setWarmPeak(null);
        }
    }

    private boolean sustained(Instant since, Instant now, int minutes) {
        return Duration.between(since, now).compareTo(Duration.ofMinutes(minutes)) >= 0;
    }
}
