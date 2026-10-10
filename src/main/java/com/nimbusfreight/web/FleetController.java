package com.nimbusfreight.web;

import com.nimbusfreight.service.FleetQueryService;
import com.nimbusfreight.service.ReadingIngestService;
import com.nimbusfreight.service.SeedReplayService;
import com.nimbusfreight.web.dto.AlertResponse;
import com.nimbusfreight.web.dto.PolicyResponse;
import com.nimbusfreight.web.dto.ReadingRequest;
import com.nimbusfreight.web.dto.ReadingResponse;
import com.nimbusfreight.web.dto.TruckSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
public class FleetController {

    private final FleetQueryService fleet;
    private final ReadingIngestService ingest;
    private final SeedReplayService replay;

    public FleetController(FleetQueryService fleet, ReadingIngestService ingest, SeedReplayService replay) {
        this.fleet = fleet;
        this.ingest = ingest;
        this.replay = replay;
    }

    @GetMapping("/api/trucks")
    public List<TruckSummaryResponse> trucks() {
        return fleet.trucks();
    }

    @GetMapping("/api/trucks/{id}")
    public TruckSummaryResponse truck(@PathVariable String id) {
        return fleet.truck(id);
    }

    @GetMapping("/api/trucks/{id}/readings")
    public List<ReadingResponse> readings(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return fleet.readings(id, from, to);
    }

    @GetMapping("/api/alerts")
    public List<AlertResponse> alerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String truckId
    ) {
        return fleet.alerts(status, truckId);
    }

    @GetMapping("/api/policy")
    public PolicyResponse policy() {
        return fleet.policy();
    }

    @PostMapping("/api/readings")
    public ReadingIngestService.IngestResult ingest(@RequestBody ReadingRequest request) {
        return ingest.ingest(request.truckId(), request.temperatureC(), request.recordedAt());
    }

    @PostMapping("/api/admin/replay-seed")
    public SeedReplayService.ReplayResult replaySeed() throws IOException {
        return replay.replay();
    }
}
