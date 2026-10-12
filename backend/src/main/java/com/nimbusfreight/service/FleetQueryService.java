package com.nimbusfreight.service;

import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.domain.AlertStatus;
import com.nimbusfreight.domain.AlertTrackState;
import com.nimbusfreight.domain.TruckStatus;
import com.nimbusfreight.entity.Alert;
import com.nimbusfreight.entity.SensorReading;
import com.nimbusfreight.entity.Truck;
import com.nimbusfreight.entity.TruckAlertStateEntity;
import com.nimbusfreight.repo.AlertRepository;
import com.nimbusfreight.repo.SensorReadingRepository;
import com.nimbusfreight.repo.TruckAlertStateRepository;
import com.nimbusfreight.repo.TruckRepository;
import com.nimbusfreight.web.TruckNotFoundException;
import com.nimbusfreight.web.dto.AlertResponse;
import com.nimbusfreight.web.dto.PolicyResponse;
import com.nimbusfreight.web.dto.ReadingResponse;
import com.nimbusfreight.web.dto.TruckSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class FleetQueryService {

    private final TruckRepository trucks;
    private final SensorReadingRepository readings;
    private final AlertRepository alerts;
    private final TruckAlertStateRepository states;
    private final AlertPolicy policy;

    public FleetQueryService(
            TruckRepository trucks,
            SensorReadingRepository readings,
            AlertRepository alerts,
            TruckAlertStateRepository states,
            AlertPolicy policy
    ) {
        this.trucks = trucks;
        this.readings = readings;
        this.alerts = alerts;
        this.states = states;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    public List<TruckSummaryResponse> trucks() {
        return trucks.findAll().stream()
                .sorted(Comparator.comparing(Truck::getId))
                .map(this::summarize)
                .toList();
    }

    @Transactional(readOnly = true)
    public TruckSummaryResponse truck(String id) {
        Truck truck = trucks.findById(id).orElseThrow(() -> new TruckNotFoundException(id));
        return summarize(truck);
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> readings(String truckId, Instant from, Instant to) {
        if (!trucks.existsById(truckId)) {
            throw new TruckNotFoundException(truckId);
        }
        List<SensorReading> rows = (from != null && to != null)
                ? readings.findByTruckIdAndRecordedAtBetweenOrderByRecordedAtAsc(truckId, from, to)
                : readings.findByTruckIdOrderByRecordedAtAsc(truckId);
        return rows.stream().map(ReadingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> alerts(String status, String truckId) {
        List<Alert> rows;
        if (truckId != null && !truckId.isBlank()) {
            rows = alerts.findByTruckIdOrderByStartedAtDesc(truckId);
        } else {
            rows = alerts.findAllByOrderByStartedAtDesc();
        }
        return rows.stream()
                .filter(alert -> status == null || status.isBlank() || alert.getStatus().name().equalsIgnoreCase(status))
                .sorted(Comparator
                        .comparing((Alert alert) -> alert.getStatus() == AlertStatus.OPEN ? 0 : 1)
                        .thenComparing(Alert::getStartedAt, Comparator.reverseOrder()))
                .map(alert -> AlertResponse.from(alert, truckName(alert.getTruckId())))
                .toList();
    }

    public PolicyResponse policy() {
        return PolicyResponse.from(policy);
    }

    private TruckSummaryResponse summarize(Truck truck) {
        SensorReading latest = readings.findFirstByTruckIdOrderByRecordedAtDesc(truck.getId()).orElse(null);
        TruckAlertStateEntity entity = states.findById(truck.getId()).orElseGet(() -> new TruckAlertStateEntity(truck.getId()));
        AlertTrackState state = entity.toState();
        BigDecimal latestTemp = latest == null ? null : latest.getTemperatureC();
        TruckStatus status = statusOf(state, latestTemp);
        Alert open = openAlert(state);
        return new TruckSummaryResponse(
                truck.getId(),
                truck.getName(),
                truck.getCargoType(),
                latestTemp,
                latest == null ? null : latest.getRecordedAt(),
                status,
                open == null ? null : AlertResponse.from(open, truck.getName())
        );
    }

    private TruckStatus statusOf(AlertTrackState state, BigDecimal latestTemp) {
        if (state.getOpenHighAlertId() != null) {
            return TruckStatus.ALERT_HIGH;
        }
        if (state.getOpenLowAlertId() != null) {
            return TruckStatus.ALERT_LOW;
        }
        if (state.getWarmSince() != null) {
            return TruckStatus.WARMING;
        }
        if (state.getFreezeSince() != null) {
            return TruckStatus.FREEZING;
        }
        if (latestTemp == null) {
            return TruckStatus.NO_DATA;
        }
        boolean inSafeBand = latestTemp.compareTo(policy.getThresholdFreezeResolveC()) >= 0
                && latestTemp.compareTo(policy.getThresholdHighC()) <= 0;
        return inSafeBand ? TruckStatus.OK : TruckStatus.WATCH;
    }

    private Alert openAlert(AlertTrackState state) {
        Long id = state.getOpenHighAlertId() != null ? state.getOpenHighAlertId() : state.getOpenLowAlertId();
        if (id == null) {
            return null;
        }
        return alerts.findById(id).orElse(null);
    }

    private String truckName(String truckId) {
        return trucks.findById(truckId).map(Truck::getName).orElse(truckId);
    }
}
