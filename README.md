# Nimbus Freight

Sustained temperature alerts for refrigerated trucks. One repository, two apps:

```text
backend/     Spring Boot API and alert engine
frontend/    React dashboard
```

The backend and frontend were developed as separate repositories. Their commits are imported under those folders, so `git log` still shows the original history.

## Quick start

```powershell
cd backend
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Without Docker, from `backend`:

```powershell
$env:JAVA_HOME = "D:\Softwares\JDK"
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 and choose **Replay seed data**.

## Documentation

| Doc | Contents |
| --- | --- |
| [DOCUMENTATION.md](DOCUMENTATION.md) | Architecture, assumptions, trade-offs, API, data model, tests |
| [JUDGE_QA.md](JUDGE_QA.md) | Judge questions, edge cases, and design trade-offs |
| [backend/SOLUTION_DESIGN.md](backend/SOLUTION_DESIGN.md) | Short hackathon solution design |
| [backend/README.md](backend/README.md) | Backend run and configuration |
| [frontend/README.md](frontend/README.md) | Frontend run |

Earlier split remotes, kept for reference:

- https://github.com/siddharth8763/nimbus-freight-backend
- https://github.com/siddharth8763/nimbus-freight-frontend
