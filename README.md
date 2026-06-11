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
psql "$DATABASE_URL" -f src/main/resources/db/auth/users_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/auth/ip_blacklist_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/rooms/rooms_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/auth/auth_data.sql
```

### 3. Build & run

```bash
./gradlew build -x test
./gradlew test                 # 77 tests
./gradlew bootRun              # → http://localhost:8080
```

## Endpoints

| Endpoint | Auth | Description |
|---|---|---|
| `POST /auth/login` | — | Authenticate admin (returns JWT) |
| `POST /auth/participant` | — | Identify or register a participant |
| `GET /rooms` | — | List all rooms (paginated, filter by name) |
| `GET /rooms/{id}` | — | Get room details |
| `POST /rooms` | JWT | Create a new room |
| `PUT /rooms/{id}` | JWT | Update a room |
| `DELETE /rooms/{id}` | JWT | Delete a room |

> See `docs/api.yaml` for the complete OpenAPI spec (all schemas, responses, and error definitions).

## Architecture

**Validation:** null/blank checks via `@NotBlank` on the DTO. Format validation delegated to `DataValidator` in the service layer — keeps validation logic testable and exception messages precise.

**Error handling:** business exceptions (`BadRequestException`, `UnprocessableEntityException`, `UnauthorizedException`, `TooManyRequestException`, `NotFoundException`, `ConflictException`) are thrown from services and handled by `GlobalExceptionHandler`. All error responses follow `{status, error, message}`.

**Authentication:** JWT extracted from `jwt` cookie (HttpOnly, Secure, SameSite=Strict). Rate-limited to 5 failed attempts per IP via `BlacklistedIp` entity.

**Persistence:** schema managed externally in `src/main/resources/db/` as plain SQL. Hibernate runs with `ddl-auto=validate`. Write queries use `INSERT ... RETURNING` / `UPDATE ... RETURNING` native queries.

---

*Java 21 · Spring Boot 4.0.6 · PostgreSQL (Neon) · Gradle 9.5.1 · Lombok · JWT (auth0)*
