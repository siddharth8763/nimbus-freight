# Nimbus Freight

Hackathon project: sustained temperature alerts for refrigerated trucks (warm + freeze), with alert fatigue handled in the rule engine.

**Submission (monorepo):** https://github.com/siddharth8763/nimbus-freight — `backend/`, `frontend/`, `starter_kit/`, design docs, and `start.ps1` in one clone.

This folder can also be laid out as **two git repositories** for day-to-day work:

| Directory | GitHub |
| --- | --- |
| [`backend/`](backend/) | https://github.com/siddharth8763/nimbus-freight-backend |
| [`frontend/`](frontend/) | https://github.com/siddharth8763/nimbus-freight-frontend |

```text
nimbus-freight/
  backend/
  frontend/
  starter_kit/          # case-study seed + alert_utils.py
  case-study-brief.md
  SOLUTION_DESIGN.md
```

## Quick start

From this folder, one command starts MySQL, the API, and the dashboard. Docker Desktop must be running, and `JAVA_HOME` must point at a JDK (the script uses it if it is already set).

```powershell
.\start.ps1
```

That opens two more windows: Spring Boot on http://localhost:8080 and Vite on http://localhost:5173. MySQL listens on `localhost:3306` (database `nimbus`, user `nimbus`, password `nimbus`). When the UI loads, choose **Replay seed data**.

To start the pieces yourself:

1. `cd backend` → `docker compose up -d` → `.\mvnw.cmd spring-boot:run`
2. `cd frontend` → `npm install` → `npm run dev`

**Documentation**

| Doc | Contents |
| --- | --- |
| [DOCUMENTATION.md](DOCUMENTATION.md) | Technical reference: architecture, assumptions, trade-offs, API, data model, testing |
| [backend/SOLUTION_DESIGN.md](backend/SOLUTION_DESIGN.md) | Hackathon solution design (concise) |
| [backend/README.md](backend/README.md) | Backend run & config |
| [frontend/README.md](frontend/README.md) | Frontend run |
