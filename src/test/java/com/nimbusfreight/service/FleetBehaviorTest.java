package com.nimbusfreight.service;

import com.nimbusfreight.domain.AlertStatus;
import com.nimbusfreight.domain.AlertType;
import com.nimbusfreight.domain.TruckStatus;
import com.nimbusfreight.engine.InvalidReadingException;
import com.nimbusfreight.repo.AlertRepository;
import com.nimbusfreight.repo.SensorReadingRepository;
import com.nimbusfreight.web.dto.AlertResponse;
import com.nimbusfreight.web.dto.TruckSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FleetBehaviorTest {

    @Autowired
    private ReadingIngestService ingest;

    @Autowired
    private SeedReplayService replay;

    @Autowired
    private FleetQueryService fleet;

    @Autowired
    private AlertRepository alerts;

    @Autowired
    private SensorReadingRepository readings;

    @Test
    void replaySeedMatchesScenariosAndIsIdempotent() throws Exception {
        SeedReplayService.ReplayResult first = replay.replay();

        assertEquals(1, first.rejectedInvalid());
        assertEquals(1, first.duplicates());
        assertEquals(0, first.storedOutOfOrder());
        assertTrue(first.processed() > 0);

        TruckSummaryResponse aurora = fleet.truck("NF-01");
        TruckSummaryResponse borealis = fleet.truck("NF-02");
        TruckSummaryResponse cirrus = fleet.truck("NF-03");
        TruckSummaryResponse drift = fleet.truck("NF-04");
        TruckSummaryResponse ember = fleet.truck("NF-05");

        assertEquals(TruckStatus.FREEZING, aurora.status());
        assertEquals(TruckStatus.ALERT_HIGH, borealis.status());
        assertEquals(TruckStatus.ALERT_LOW, cirrus.status());
        assertEquals(TruckStatus.WARMING, drift.status());
        assertEquals(TruckStatus.ALERT_HIGH, ember.status());

        List<AlertResponse> borealisAlerts = fleet.alerts(null, "NF-02");
        assertEquals(2, borealisAlerts.size());
        AlertResponse borealisOpen = borealisAlerts.stream().filter(alert -> alert.status() == AlertStatus.OPEN).findFirst().orElseThrow();
        AlertResponse borealisResolved = borealisAlerts.stream().filter(alert -> alert.status() == AlertStatus.RESOLVED).findFirst().orElseThrow();
        assertDecimal("8.6", borealisOpen.peakTempC());
        assertDecimal("9.2", borealisResolved.peakTempC());

        List<AlertResponse> cirrusAlerts = fleet.alerts(null, "NF-03");
        assertEquals(1, cirrusAlerts.size());
        assertEquals(AlertType.LOW_TEMP, cirrusAlerts.get(0).type());
        assertDecimal("0.2", cirrusAlerts.get(0).minTempC());

        assertTrue(fleet.alerts(null, "NF-01").isEmpty());
        assertTrue(fleet.alerts(null, "NF-04").isEmpty());
        assertEquals(1, fleet.alerts(null, "NF-05").size());
        assertEquals(AlertType.HIGH_TEMP, ember.openAlert().type());

        long alertCount = alerts.count();
        long readingCount = readings.count();

        SeedReplayService.ReplayResult second = replay.replay();
        assertEquals(0, second.processed());
        assertEquals(first.processed() + first.duplicates(), second.duplicates());
        assertEquals(alertCount, alerts.count());
        assertEquals(readingCount, readings.count());
    }

    @Test
    void duplicateTimestampIsIgnored() {
        Instant when = Instant.parse("2026-10-02T00:00:00Z");
        assertEquals(ReadingIngestService.IngestResult.PROCESSED, ingest.ingest("NF-01", new BigDecimal("4.0"), when));
        assertEquals(ReadingIngestService.IngestResult.DUPLICATE, ingest.ingest("NF-01", new BigDecimal("12.0"), when));
        assertEquals(1, readings.findByTruckIdOrderByRecordedAtAsc("NF-01").size());
        assertEquals(0, new BigDecimal("4.0").compareTo(readings.findByTruckIdOrderByRecordedAtAsc("NF-01").get(0).getTemperatureC()));
    }

    @Test
    void outOfOrderReadingIsStoredButDoesNotMoveState() {
        Instant later = Instant.parse("2026-10-02T02:00:00Z");
        Instant earlier = Instant.parse("2026-10-02T01:00:00Z");
        ingest.ingest("NF-01", new BigDecimal("4.0"), later);
        assertEquals(ReadingIngestService.IngestResult.STORED_OUT_OF_ORDER, ingest.ingest("NF-01", new BigDecimal("20.0"), earlier));

        assertEquals(2, readings.findByTruckIdOrderByRecordedAtAsc("NF-01").size());
        assertTrue(alerts.findByTruckIdOrderByStartedAtDesc("NF-01").isEmpty());
        assertEquals(TruckStatus.OK, fleet.truck("NF-01").status());
    }

    @Test
    void invalidReadingIsRejected() {
        assertThrows(InvalidReadingException.class,
                () -> ingest.ingest("NF-01", new BigDecimal("999"), Instant.parse("2026-10-02T03:00:00Z")));
        assertTrue(readings.findByTruckIdOrderByRecordedAtAsc("NF-01").isEmpty());
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
