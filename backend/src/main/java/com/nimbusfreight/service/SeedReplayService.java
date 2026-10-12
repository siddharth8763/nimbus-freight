package com.nimbusfreight.service;

import com.nimbusfreight.engine.InvalidReadingException;
import com.nimbusfreight.service.ReadingIngestService.IngestResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SeedReplayService {

    private final ReadingIngestService ingest;
    private final String seedPath;

    public SeedReplayService(ReadingIngestService ingest, @Value("${nimbus.seed-path}") String seedPath) {
        this.ingest = ingest;
        this.seedPath = seedPath;
    }

    public ReplayResult replay() throws IOException {
        List<SeedRow> rows = loadRows();
        rows.sort(Comparator.comparing(SeedRow::recordedAt).thenComparing(SeedRow::truckId));

        int processed = 0;
        int duplicates = 0;
        int storedOutOfOrder = 0;
        int rejectedInvalid = 0;

        for (SeedRow row : rows) {
            try {
                IngestResult result = ingest.ingest(row.truckId(), row.temperatureC(), row.recordedAt());
                switch (result) {
                    case PROCESSED -> processed++;
                    case DUPLICATE -> duplicates++;
                    case STORED_OUT_OF_ORDER -> storedOutOfOrder++;
                }
            } catch (InvalidReadingException ex) {
                rejectedInvalid++;
            }
        }
        return new ReplayResult(rows.size(), processed, duplicates, storedOutOfOrder, rejectedInvalid);
    }

    private List<SeedRow> loadRows() throws IOException {
        try (BufferedReader reader = open()) {
            List<SeedRow> rows = new ArrayList<>();
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (header && trimmed.toLowerCase().startsWith("truck_id")) {
                    header = false;
                    continue;
                }
                header = false;
                String[] parts = trimmed.split(",");
                if (parts.length < 3) {
                    continue;
                }
                rows.add(new SeedRow(
                        parts[0].trim(),
                        new BigDecimal(parts[1].trim()),
                        Instant.parse(parts[2].trim())
                ));
            }
            return rows;
        }
    }

    private BufferedReader open() throws IOException {
        if (seedPath.startsWith("classpath:")) {
            String location = seedPath.substring("classpath:".length());
            return new BufferedReader(new InputStreamReader(
                    new ClassPathResource(location).getInputStream(), StandardCharsets.UTF_8));
        }
        Path configured = Path.of(seedPath);
        if (Files.exists(configured)) {
            return Files.newBufferedReader(configured);
        }
        Path fromRepoRoot = Path.of("data/seed/readings.csv");
        if (Files.exists(fromRepoRoot)) {
            return Files.newBufferedReader(fromRepoRoot);
        }
        Path fromBackendModule = Path.of("../data/seed/readings.csv");
        if (Files.exists(fromBackendModule)) {
            return Files.newBufferedReader(fromBackendModule);
        }
        return new BufferedReader(new InputStreamReader(
                new ClassPathResource("seed/readings.csv").getInputStream(), StandardCharsets.UTF_8));
    }

    private record SeedRow(String truckId, BigDecimal temperatureC, Instant recordedAt) {
    }

    public record ReplayResult(
            int rowsRead,
            int processed,
            int duplicates,
            int storedOutOfOrder,
            int rejectedInvalid
    ) {
    }
}
