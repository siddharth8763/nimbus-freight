# Nimbus Freight — API

Spring Boot service for sustained reefer temperature alerts (`HIGH_TEMP` and `LOW_TEMP`). Pair with the [frontend repo](https://github.com/siddharth8763/nimbus-freight-frontend).

Design notes: [SOLUTION_DESIGN.md](SOLUTION_DESIGN.md).

## Prerequisites

- Java 17+ (`JAVA_HOME` set to your JDK)
- Docker (MySQL 8)

## Run

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

API: http://localhost:8080 — replay seed with `POST /api/admin/replay-seed`.

## Configuration

Thresholds and seed path: `src/main/resources/application.yml` (`nimbus.alert.*`, `nimbus.seed-path`).

Optional alert summaries: set `OPENAI_API_KEY` before starting the app.

## Tests

```powershell
.\mvnw.cmd test
```

## Seed data

`data/seed/readings.csv` (also under `src/main/resources/seed/` for the classpath).
