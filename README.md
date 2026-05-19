# IncidentIQ

A production-grade incident management and learning API built with Java 21 and Spring Boot 3. Teams use IncidentIQ to capture incidents, build structured timelines, and generate AI-written post-mortems — turning operational pain into institutional knowledge.

## Features

- **Incident lifecycle** — P0–P3 severity, OPEN → RESOLVED → POST_MORTEM_PENDING → CLOSED
- **Structured timelines** — ordered event log with author attribution
- **AI post-mortems** — async Claude-powered generation: root cause, contributing factors, impact, timeline narrative, lessons learned
- **Action item tracking** — per-incident follow-ups with owner, due date, and completion status
- **Pattern detection** — scheduled daily AI analysis surfaces recurring themes and systemic gaps across the last 30 days of incidents
- **OpenAPI / Swagger UI** — interactive docs at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Persistence | Spring Data JPA + Hibernate + PostgreSQL |
| Migrations | Flyway |
| AI | Anthropic Claude API via Spring RestClient |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Dev | Docker Compose |

## Quick Start

**1. Start the database**
```bash
docker compose up -d
```

**2. Configure environment**
```bash
cp .env.example .env
# Edit .env and set AI_API_KEY=your_anthropic_key
```

**3. Run the API**
```bash
./mvnw spring-boot:run
```

API runs at `http://localhost:8080`  
Swagger UI at `http://localhost:8080/swagger-ui.html`

## API Overview

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/incidents` | List incidents (filter by `status`, `severity`) |
| `POST` | `/api/incidents` | Create incident |
| `GET` | `/api/incidents/{id}` | Get incident |
| `POST` | `/api/incidents/{id}/resolve` | Mark resolved |
| `POST` | `/api/incidents/{id}/close` | Close incident |
| `GET` | `/api/incidents/{id}/timeline` | Get timeline |
| `POST` | `/api/incidents/{id}/timeline` | Add timeline event |
| `GET` | `/api/incidents/{id}/action-items` | List action items |
| `POST` | `/api/incidents/{id}/action-items` | Add action item |
| `POST` | `/api/incidents/action-items/{id}/complete` | Complete action item |
| `POST` | `/api/incidents/{id}/postmortem/generate` | Trigger AI post-mortem (async, returns 202) |
| `GET` | `/api/incidents/{id}/postmortem` | Get post-mortem (poll status: GENERATING → READY) |
| `GET` | `/api/patterns` | Latest pattern analysis |
| `POST` | `/api/patterns/analyze` | Run analysis now |

## Example: Full Incident Lifecycle

```bash
# 1. Open an incident
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Checkout service latency spike",
    "severity": "P1",
    "affectedServices": "checkout-api, payment-gateway",
    "detectedAt": "2025-05-19T14:30:00Z"
  }'

# 2. Add timeline events
curl -X POST http://localhost:8080/api/incidents/1/timeline \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Latency p99 crossed 5s threshold, alerts fired",
    "author": "ops-bot",
    "occurredAt": "2025-05-19T14:30:00Z"
  }'

# 3. Resolve it
curl -X POST http://localhost:8080/api/incidents/1/resolve

# 4. Generate AI post-mortem
curl -X POST http://localhost:8080/api/incidents/1/postmortem/generate

# 5. Poll until READY
curl http://localhost:8080/api/incidents/1/postmortem
```

## Architecture

**Async post-mortem generation** — `POST /generate` creates a `PostMortem` record in `GENERATING` state and returns `202 Accepted` immediately. Spring `@Async` runs the Claude API call in a thread pool; on completion the record transitions to `READY` (or `FAILED` with a failure message). Clients poll `GET /postmortem`.

**Pattern detection** — `PatternService` runs on a daily cron (`0 0 2 * * *`), fetches resolved incidents from the last 30 days, and calls Claude to surface recurring themes, root cause categories, and systemic gaps. Results are cached in-memory and exposed via `GET /api/patterns`.

**Error responses** — all errors use [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).
