# EventSync-API

Event management API with real-time participant engagement.

## Prerequisites

- **Java 21** — Gradle toolchain auto-resolves a local JDK 21 (JVM or GraalVM).
- **PostgreSQL 14+** — A running instance (local or [Neon](https://neon.tech) serverless).
- **Gradle** — Wrapper included (`./gradlew`), no system-wide install needed.

## Setup

### 1. Environment variables

Create a `.env` file at the project root:

```bash
PGHOST=ep-xxx-pooler.eu-west-2.aws.neon.tech
PGDATABASE=eventsync_db
PGUSER=eventsync_manager
PGPASSWORD=your-db-password
JWT_SECRET=a-long-random-secret-key
PGSSLMODE=require
CORS_ALLOWED_ORIGINS=http://localhost:5173
AI_API_KEY=your-openai-compatible-api-key
AI_MODEL=big-pickle
```

> `.env` is gitignored. The `application.properties` reads it via `spring.config.import=optional:file:.env[.properties]`.
>
> **Important:** The property key is `JWT_SECRET` (not `JWT_TOKEN`). The security config references `${security.jwt.token.secret-key}` which resolves from `JWT_SECRET`. Using `JWT_TOKEN` will silently fall back to no secret.
>
> **AI variables** (`AI_API_KEY`, `AI_MODEL`) are optional — only needed if you use the `/ai/conversations` endpoint.

### 2. Create the database schema

The project uses `spring.jpa.hibernate.ddl-auto=validate`, meaning **you** manage the schema externally. Apply the SQL scripts in dependency order:

```bash
# 1. Auth (no foreign keys)
psql "$DATABASE_URL" -f src/main/resources/db/auth/users_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/auth/ip_blacklist_schema.sql

# 2. Events, rooms, external_links (independent)
psql "$DATABASE_URL" -f src/main/resources/db/events/events_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/rooms/rooms_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/externalLink/external_links_schema.sql

# 3. Sessions (fk → room, event)
psql "$DATABASE_URL" -f src/main/resources/db/sessions/sessions_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/sessions/session_speaker_schema.sql

# 4. Questions and upvotes (fk → session, \"user\")
psql "$DATABASE_URL" -f src/main/resources/db/questions/question_schema.sql
psql "$DATABASE_URL" -f src/main/resources/db/upvote/upvote_schema.sql

# 5. AI conversations (fk → \"user\")
psql "$DATABASE_URL" -f src/main/resources/db/ai_conversations/ai_conversations_schema.sql
```

Where `$DATABASE_URL` is a PostgreSQL connection string:

```bash
export DATABASE_URL="postgresql://${PGUSER}:***@${PGHOST}:5432/${PGDATABASE}?sslmode=${PGSSLMODE}"
```

### 3. Seed test data (optional)

```bash
# Auth — admin user for login
psql "$DATABASE_URL" -f src/main/resources/db/auth/auth_data.sql

# Sessions — room, event, speaker, and two sessions
psql "$DATABASE_URL" -f src/main/resources/db/sessions/test_session_data.sql
```

Additional per-endpoint seed files exist under `src/main/resources/db/` for testing specific scenarios:

| File | Purpose |
|---|---|
| `db/events/put_event_data.sql` | Room + event for PUT /events/{id} tests |
| `db/events/get_events_data.sql` | Sample events for GET /events |
| `db/events/get_event_by_id_data.sql` | Room + event + session for GET /events/{id} |
| `db/events/delete_event_data.sql` | Event + room + session for DELETE /events/{id} |
| `db/rooms/put_room_data.sql` | Room for PUT /rooms/{id} |
| `db/rooms/get_rooms_data.sql` | Sample rooms |
| `db/rooms/get_room_by_id_data.sql` | Room for GET /rooms/{id} |
| `db/rooms/delete_room_data.sql` | Room + linked session for DELETE /rooms/{id} |
| `db/sessions/put_session_data.sql` | Room + event + speaker + sessions for PUT /sessions/{id} |
| `db/sessions/delete_session_data.sql` | Room + event + session for DELETE /sessions/{id} |
| `db/speaker/put_speaker_data.sql` | Speaker user for PUT /speakers/{id} |
| `db/speaker/get_speaker_by_id_data.sql` | Speaker + external links for GET /speakers/{id} |
| `db/speaker/delete_speaker_data.sql` | Speaker + external links for DELETE /speakers/{id} |
| `db/externalLink/put_speaker_external_link_data.sql` | Speaker + external link for PUT /speakers/{id}/external-link |
| `db/externalLink/post_external_link_data.sql` | Speaker data for POST /speakers/{id}/external-link |
| `db/externalLink/delete_external_link_data.sql` | Speaker + external link for DELETE /speakers/{id}/external-link |
| `db/questions/post_question_data.sql` | Session + user for POST /sessions/{id}/questions |

### 4. Build and run

```bash
# Compile only (fast)
./gradlew compileJava

# Run all tests
./gradlew test

# Full build without tests
./gradlew build -x test

# Start the server → http://localhost:8080
./gradlew bootRun
```

## Endpoints

### Auth

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/login` | — | Login as admin (returns JWT in `jwt` cookie) |
| `POST` | `/auth/participant` | — | Identify or register a participant |

### Events

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/events` | — | List events (pagination, filters: `title`, `location`, `startDate`, `endDate`, `isLive`) |
| `POST` | `/events` | JWT ADMIN | Create event |
| `GET` | `/events/{id}` | — | Get event details (includes nested sessions) |
| `PUT` | `/events/{id}` | JWT ADMIN | Update event |
| `DELETE` | `/events/{id}` | JWT ADMIN | Delete event |

### Rooms

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/rooms` | — | List rooms (pagination, filter: `name`) |
| `POST` | `/rooms` | JWT ADMIN | Create room |
| `GET` | `/rooms/{id}` | — | Get room by UUID |
| `PUT` | `/rooms/{id}` | JWT ADMIN | Update room |
| `DELETE` | `/rooms/{id}` | JWT ADMIN | Delete room |

### Sessions

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/sessions` | — | List sessions (pagination, filters: `room`, `speaker`, `live`, `event`) |
| `POST` | `/sessions` | JWT ADMIN | Create session (linked to room + event, optional speakers) |
| `GET` | `/sessions/{id}` | — | Get session details (includes speakers and questions) |
| `PUT` | `/sessions/{id}` | JWT ADMIN | Update session (all fields mandatory) |
| `DELETE` | `/sessions/{id}` | JWT ADMIN | Delete session |
| `GET` | `/sessions/{id}/questions` | — | List questions for a session (pagination, sort by `upvotes` or `createdAt`, filter by `title`) |
| `POST` | `/sessions/{id}/questions` | JWT (any role) | Post a question to a session (title, content, optional `isAnonymous`) |

### Speakers

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/speakers` | — | List speakers (pagination, filter: `search` by name) |
| `POST` | `/speakers` | JWT ADMIN | Create speaker (with optional external links) |
| `GET` | `/speakers/{id}` | — | Get speaker details (includes external links and sessions) |
| `PUT` | `/speakers/{id}` | JWT ADMIN | Update speaker (firstName, lastName, email, optional bio/profilePicture) |
| `DELETE` | `/speakers/{id}` | JWT ADMIN | Delete speaker (cascades external links) |
| `POST` | `/speakers/{id}/external-link` | JWT ADMIN | Add an external link to a speaker |
| `PUT` | `/speakers/{id}/external-link` | JWT ADMIN | Update an external link by `urlName` query param |
| `DELETE` | `/speakers/{id}/external-link` | JWT ADMIN | Delete an external link by `externalLinkId` query param |

### AI / MCP

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/ai/conversations` | JWT ADMIN | Send a message to the AI assistant (room operations via natural language) |
| `GET` | `/ai/conversations` | JWT ADMIN | List AI conversations for the authenticated user |
| `GET` | `/ai/conversations/{id}` | JWT ADMIN | Get AI conversation details |
| `DELETE` | `/ai/conversations/{id}` | JWT ADMIN | Delete AI conversation |

> See `docs/api.yaml` for the complete OpenAPI spec (all schemas, responses, and error definitions).

## Integration test scripts

Shell scripts using `curlie` are in `scripts/`. Each script is self-contained: it authenticates, sends requests, and displays responses with status codes.

| Script | Covers |
|---|---|
| `scripts/auth/test_post_auth_login.sh` | POST /auth/login |
| `scripts/event/test_post_event.sh` | POST /events |
| `scripts/event/test_put_event.sh` | PUT /events/{id} |
| `scripts/event/test_get_events.sh` | GET /events |
| `scripts/event/test_get_event_by_id.sh` | GET /events/{id} |
| `scripts/event/test_delete_event.sh` | DELETE /events/{id} |
| `scripts/room/test_post_room.sh` | POST /rooms |
| `scripts/room/test_put_room.sh` | PUT /rooms/{id} |
| `scripts/room/test_get_rooms.sh` | GET /rooms |
| `scripts/room/test_get_room_by_id.sh` | GET /rooms/{id} |
| `scripts/room/test_delete_room.sh` | DELETE /rooms/{id} |
| `scripts/sessions/test_post_sessions.sh` | POST /sessions |
| `scripts/sessions/test_put_sessions.sh` | PUT /sessions/{id} |
| `scripts/sessions/test_delete_session.sh` | DELETE /sessions/{id} |
| `scripts/speaker/test_post_speaker.sh` | POST /speakers |
| `scripts/speaker/test_put_speaker.sh` | PUT /speakers/{id} |
| `scripts/speaker/test_get_speakers.sh` | GET /speakers |
| `scripts/speaker/test_get_speaker_by_id.sh` | GET /speakers/{id} |
| `scripts/speaker/test_delete_speaker.sh` | DELETE /speakers/{id} |
| `scripts/external-link/test_post_external_link.sh` | POST /speakers/{id}/external-link |
| `scripts/external-link/test_put_speaker_external_link.sh` | PUT /speakers/{id}/external-link |
| `scripts/external-link/test_delete_external_link.sh` | DELETE /speakers/{id}/external-link |
| `scripts/questions/test_post_questions.sh` | POST /sessions/{id}/questions |

To run a script, start the server first (`./gradlew bootRun`), then:

```bash
scripts/speaker/test_get_speakers.sh
```

Each script requires `curlie` (`brew install curlie` or `apt install curlie`). They store JWT cookies in `/tmp/` and clean up on exit.

## Architecture

**Validation:** Handled entirely in the service layer via `DataValidator` (and domain-specific validators for sessions, events, speakers, external links, AI conversations, and questions). DTOs are plain `@Data` beans — no `@NotBlank` or `@Valid` annotations. Keeps validation logic testable, exception messages precise, and error handling uniform.

**Error handling:** Business exceptions (`BadRequestException`, `UnprocessableEntityException`, `UnauthorizedException`, `TooManyRequestException`, `NotFoundException`, `ConflictException`) are thrown from services and caught by `GlobalExceptionHandler`. All error responses follow `{status, error, message}`.

**Authentication:** JWT extracted from `jwt` cookie (set as response cookie on login). Rate-limited to 5 failed attempts per IP via `BlacklistedIp` entity. Public GET endpoints validate IP blacklist via `AuthService.checkBlacklist()`.

**Authorization:** Role-based (`ADMIN` / `PARTICIPANT` / `SPEAKER`). Write endpoints (POST/PUT/DELETE for events, rooms, sessions, speakers) require `ROLE_ADMIN`. Question posting requires any authenticated role. Read endpoints are public.

**Persistence:** Schema managed externally in `src/main/resources/db/` as plain SQL. Hibernate runs with `ddl-auto=validate`. Write queries use `INSERT ... RETURNING` / `UPDATE ... RETURNING` native queries with `ON CONFLICT` for idempotent inserts. Columns are always listed explicitly — no `SELECT *`.

**SecurityConfig:** Every controller path needs explicit rules for all 4 HTTP methods (GET `permitAll`, POST/PUT/DELETE `hasRole("ADMIN")`). The MCP SSE endpoint (`/mcp/**`) is `permitAll` for all methods. Questions follow `POST /sessions/{id}/questions` as `authenticated()` (any role can post).

## AI / MCP Integration

The API exposes an **MCP (Model Context Protocol) server** over SSE transport at `/mcp/sse`. MCP-compatible AI agents can discover and invoke business operations via natural language.

The server auto-configures via `spring-ai-starter-mcp-server-webmvc` — no separate process needed. All `/mcp/**` paths use `permitAll` in `SecurityConfig`.

### Available MCP tools

| Tool | Description |
|---|---|
| `createRoom` | Create a new room |
| `listRooms` | List rooms with optional search and pagination |
| `getRoom` | Get room details by UUID |
| `updateRoom` | Update an existing room |
| `deleteRoom` | Delete a room by UUID |

Each tool wraps the existing service layer — validation, error handling, and business rules are identical to the REST API. Tools use `@ToolParam(description = ...)` so the AI knows what each parameter means.

---

*Java 21 · Spring Boot 4.0.6 · PostgreSQL (Neon) · Gradle 9.5.1 · Lombok · JWT (auth0)*
