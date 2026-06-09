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
./gradlew test                 # 25 tests
./gradlew bootRun              # → http://localhost:8080
```

### 4. Test the login endpoint

```bash
src/main/java/com/techindna/eventsyncapi/script/post_auth_login.sh
```

## Endpoints

| Endpoint | Description |
|---|---|
| `POST /auth/login` | Authenticate admin (returns JWT cookie + token body) |

> See `docs/api.yaml` for full spec.

## Architecture

**Validation:** null/blank checks via `@NotBlank` on the DTO. Format validation delegated to `DataValidator` in the service layer — keeps validation logic testable and exception messages precise.

**Error handling:** business exceptions (`BadRequestException`, `UnprocessableEntityException`, `UnauthorizedException`, `TooManyRequestException`) are thrown from services and handled by `GlobalExceptionHandler`. All error responses follow `{status, error, message}`.

**Authentication:** JWT extracted from `jwt` cookie (HttpOnly, Secure, SameSite=Strict). Rate-limited to 5 failed attempts per IP via `BlacklistedIp` entity.

---

*Java 21 · Spring Boot 4.0.6 · PostgreSQL (Neon) · Gradle · Lombok · JWT*
