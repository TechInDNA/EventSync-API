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
PGSSLMODE=require
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> `.env` is gitignored.

### 2. Create the schema

```bash
# Auth
psql "$DATABASE_URL" -f src/main/resources/db/auth/users_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/auth/ip_blacklist_schema.sql

# Events
psql "$DATABASE_URL" -f src/main/resources/db/events/events_schema.sql

# Rooms
psql "$DATABASE_URL" -f src/main/resources/db/rooms/rooms_schema.sql

# Sessions & speakers
psql "$DATABASE_URL" -f src/main/resources/db/sessions/sessions_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/sessions/session_speaker_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/externalLink/external_links_schema.sql

# Seed data (optional — for testing)
psql "$DATABASE_URL" -f src/main/resources/db/auth/auth_data.sql
psql "$DATABASE_URL" -f src/main/resources/db/sessions/test_session_data.sql
```

### 3. Build & run

```bash
./gradlew build -x test
./gradlew test                 # 171 tests
./gradlew bootRun              # → http://localhost:8080
```

## Endpoints

| Endpoint | Auth | Description |
|---|---|---|
| `POST /auth/login` | — | Authenticate admin (returns JWT) |
| `POST /auth/participant` | — | Identify or register a participant |
| `GET /events` | — | List all events (paginated, filters: title, location, startDate, endDate, isLive) |
| `POST /events` | JWT | Create a new event |
| `PUT /events/{id}` | JWT | Update an event |
| `DELETE /events/{id}` | JWT | Delete an event |
| `GET /rooms` | — | List all rooms (paginated, filter by name) |
| `GET /rooms/{id}` | — | Get room details |
| `POST /rooms` | JWT | Create a new room |
| `PUT /rooms/{id}` | JWT | Update a room |
| `DELETE /rooms/{id}` | JWT | Delete a room |
| `POST /sessions` | JWT | Create a session (linked to room + event) |
| `POST /speakers` | JWT | Create a speaker (with optional external links) |

> See `docs/api.yaml` for the complete OpenAPI spec (all schemas, responses, and error definitions).

## Architecture

**Validation:** Handled entirely in the service layer via `DataValidator` (and `SessionValidator` for sessions, `EventValidator` for events). DTOs are plain `@Data` beans — no `@NotBlank` or `@Valid` annotations. Keeps validation logic testable, exception messages precise, and error handling uniform.

**Error handling:** business exceptions (`BadRequestException`, `UnprocessableEntityException`, `UnauthorizedException`, `TooManyRequestException`, `NotFoundException`, `ConflictException`) are thrown from services and handled by `GlobalExceptionHandler`. All error responses follow `{status, error, message}`.

**Authentication:** JWT extracted from `jwt` cookie (HttpOnly, Secure, SameSite=Strict). Rate-limited to 5 failed attempts per IP via `BlacklistedIp` entity. GET endpoints (events, rooms) validate IP blacklist via `AuthService.checkBlacklist()` to block banned IPs from public resources.

**Authorization:** Role-based (`ADMIN` / `PARTICIPANT` / `SPEAKER`). Write endpoints (POST/PUT/DELETE for events, rooms, sessions, speakers) require `ROLE_ADMIN`. Read endpoints are public.

**Persistence:** schema managed externally in `src/main/resources/db/` as plain SQL. Hibernate runs with `ddl-auto=validate`. Write queries use `INSERT ... RETURNING` / `UPDATE ... RETURNING` native queries with `ON CONFLICT` for idempotent inserts (event title, session title, external link URL).

**Event model:** Full CRUD via `EventRepository` native queries. Title is unique (`ON CONFLICT (title) DO NOTHING`). `EventValidator` validates required fields and ensures `endDate` is after `startDate`. `EventMapper` computes `isLive` and builds detail responses with nested sessions for `POST`/`PUT` endpoints.

**Session model:** Each session is linked to a room and an event via UUID references. Supports ManyToMany speakers via the `session_speaker` join table. Computed `isLive` field (between startDate/endDate) set by `SessionMapper`.

**Speaker model:** Users with role `SPEAKER` and optional `externalLinks` (name + URL). Links are stored in `external_links` table per-row with `ON CONFLICT (url)` guarding against duplicates.

## AI / MCP Integration

The API exposes an **MCP (Model Context Protocol) server** over SSE transport, allowing AI agents like [Hermes Agent](https://hermes-agent.nousresearch.com) to discover and invoke business operations via natural language.

### Available MCP tools

| Tool | Description |
|---|---|
| `createRoom` | Create a new room (name) |
| `listRooms` | List rooms with optional search and pagination |
| `getRoom` | Get room details by UUID |
| `updateRoom` | Update an existing room (name) |
| `deleteRoom` | Delete a room by UUID |

Each tool wraps the existing service layer — validation, error handling, and business rules are identical to the REST API.

### Configure Hermes Agent to connect

1. Ensure the API is running (`./gradlew bootRun`).
2. Add the MCP server to your Hermes `~/.hermes/config.yaml`:

```yaml
mcp_servers:
  eventsync:
    transport: sse
    url: "http://localhost:8080/mcp/sse"
    timeout: 30
```

3. Start a new Hermes session or reload existing ones with `/reload-mcp`.

Hermes discovers the tools and the AI can now run operations like:

> *"Create a room called Conference Hall A"* → Hermes calls `createRoom(name="Conference Hall A")` → returns success or error details.

### How it works

```
User message
    → Hermes AI (LLM decides to call a tool)
    → SSE POST /mcp/message
    → Spring Boot @Tool bean (e.g. RoomMcpTools.createRoom)
    → RoomService → Database
    → Tool result returned over SSE
    → Hermes reports back to user
```

- The MCP server runs inside the Spring Boot process — no separate service needed.
- The `/mcp/sse` and `/mcp/message` endpoints are auto-configured by `spring-ai-starter-mcp-server-webmvc` and are not JWT-protected (use `permitAll` in `SecurityConfig`).
- Tools use `@ToolParam(description = ...)` so the AI knows what each parameter means.

---

*Java 21 · Spring Boot 4.0.6 · PostgreSQL (Neon) · Gradle 9.5.1 · Lombok · JWT (auth0)*
