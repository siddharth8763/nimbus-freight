package com.nimbusfreight.service;

import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.domain.AlertSink;
import com.nimbusfreight.domain.AlertTrackState;
import com.nimbusfreight.domain.AlertType;
import com.nimbusfreight.domain.ResolutionReason;
import com.nimbusfreight.engine.InvalidReadingException;
import com.nimbusfreight.engine.TemperatureAlertEngine;
import com.nimbusfreight.entity.Alert;
import com.nimbusfreight.entity.SensorReading;
import com.nimbusfreight.entity.Truck;
import com.nimbusfreight.entity.TruckAlertStateEntity;
import com.nimbusfreight.repo.AlertRepository;
import com.nimbusfreight.repo.SensorReadingRepository;
import com.nimbusfreight.repo.TruckAlertStateRepository;
import com.nimbusfreight.repo.TruckRepository;
import com.nimbusfreight.web.TruckNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ReadingIngestService {

    private final TruckRepository trucks;
    private final SensorReadingRepository readings;
    private final AlertRepository alerts;
    private final TruckAlertStateRepository states;
    private final TemperatureAlertEngine engine;
    private final AlertPolicy policy;
    private final AlertSummaryService summaries;

    public ReadingIngestService(
            TruckRepository trucks,
            SensorReadingRepository readings,
            AlertRepository alerts,
            TruckAlertStateRepository states,
            TemperatureAlertEngine engine,
            AlertPolicy policy,
            AlertSummaryService summaries
    ) {
        this.trucks = trucks;
        this.readings = readings;
        this.alerts = alerts;
        this.states = states;
        this.engine = engine;
        this.policy = policy;
        this.summaries = summaries;
    }

    @Transactional
    public IngestResult ingest(String truckId, BigDecimal temperatureC, Instant recordedAt) {
        if (truckId == null || truckId.isBlank()) {
            throw new InvalidReadingException("truckId is required");
        }
        if (temperatureC == null
                || temperatureC.compareTo(policy.getMinValidTempC()) < 0
                || temperatureC.compareTo(policy.getMaxValidTempC()) > 0) {
            throw new InvalidReadingException(
                    "Temperature must be between " + policy.getMinValidTempC() + " and " + policy.getMaxValidTempC() + " °C");
        }
        if (recordedAt == null) {
            throw new InvalidReadingException("recordedAt is required");
        }
        Truck truck = trucks.findById(truckId).orElseThrow(() -> new TruckNotFoundException(truckId));
        if (readings.existsByTruckIdAndRecordedAt(truckId, recordedAt)) {
            return IngestResult.DUPLICATE;
        }

        try {
            readings.saveAndFlush(new SensorReading(truckId, temperatureC, recordedAt));
        } catch (DataIntegrityViolationException ex) {
            return IngestResult.DUPLICATE;
        }

        TruckAlertStateEntity entity = states.findById(truckId).orElseGet(() -> new TruckAlertStateEntity(truckId));
        AlertTrackState state = entity.toState();
        if (state.getLastProcessedAt() != null && !recordedAt.isAfter(state.getLastProcessedAt())) {
            return IngestResult.STORED_OUT_OF_ORDER;
        }

        engine.evaluate(state, recordedAt, temperatureC, policy, new PersistingSink(truck));
        entity.apply(state);
        states.save(entity);
        return IngestResult.PROCESSED;
    }

    public enum IngestResult {
        PROCESSED,
        DUPLICATE,
        STORED_OUT_OF_ORDER
    }

    private final class PersistingSink implements AlertSink {
        private final Truck truck;

        private PersistingSink(Truck truck) {
            this.truck = truck;
        }

        @Override
        public long open(AlertType type, Instant startedAt, BigDecimal extremeTemp) {
            String message = summaries.summarize(
                    truck.getName(), type, extremeTemp, policy.getSustainDurationMinutes());
            Alert alert = alerts.saveAndFlush(new Alert(truck.getId(), type, startedAt, extremeTemp, message));
            return alert.getId();
        }

        @Override
        public void resolve(long alertId, Instant resolvedAt, ResolutionReason reason) {
            Alert alert = alerts.findById(alertId).orElseThrow();
            alert.resolve(resolvedAt, reason);
        }

        @Override
        public void updateExtreme(long alertId, BigDecimal temp, AlertType type) {
            alerts.findById(alertId).orElseThrow().noteExtreme(temp);
        }
    }
}
