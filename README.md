# EventSync-API

Event management API with real-time participant engagement.

## Prerequisites

- Java 21
- PostgreSQL 14+ (or Neon account)
- `./gradlew` (wrapper included)

## Setup

### 1. Configure `.env`

```bash
PGHOST=ep-xxx-pooler.eu-west-2.aws.neon.tech
PGDATABASE=eventsync_db
PGUSER=eventsync_manager
PGPASSWORD=...
JWT_TOKEN=your-secret-key
```

> `.env` is gitignored.

### 2. Create the schema

```bash
psql "$DATABASE_URL" -f src/main/resources/db/001_create_users.sql
```

### 3. Build & run

```bash
./gradlew build -x test
./gradlew test
./gradlew bootRun          # → http://localhost:8080
```

## Endpoints

| Endpoint | Description |
|---|---|
| `POST /api/auth/login`       | Authenticate admin |
| `POST /api/auth/participant` | Identify/register participant |
| `POST /api/auth/register`    | Create admin account |

> See `docs/api.yaml` for full spec.

---

*Java 21 · Spring Boot 4.0.6 · PostgreSQL (Neon) · Gradle · Lombok · JWT*
