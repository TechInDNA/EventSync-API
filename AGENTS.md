# EventSync-API

## Stack

- **Language** — Java 21
- **Framework** — Spring Boot 4.0.6 (webmvc, data-jpa, security)
- **Database** — PostgreSQL (Neon), schema `eventsync_app`
- **Build** — Gradle 9.5.1 (wrapper: `./gradlew`)
- **Auth** — JWT (auth0/java-jwt 4.5.2), Argon2 password encoder
- **AI** — Spring AI MCP Server (webmvc)
- **Lombok** — compileOnly + annotationProcessor

## Project structure

```
src/main/java/com/techindna/eventsyncapi/
├── EventSyncApiApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── TokenProvider.java
│   └── JwtAuthenticationFilter.java
├── controller/
│   ├── AuthController.java
│   ├── EventController.java
│   ├── RoomController.java
│   ├── SessionController.java
│   └── SpeakerController.java
├── dto/
│   ├── MetaDto.java
│   ├── auth/
│   │   ├── AuthLoginRequestDto.java
│   │   ├── AuthLoginResponseDto.java
│   │   ├── AuthParticipantRequestDto.java
│   │   ├── AuthParticipantResponseDto.java
│   │   ├── ParticipantRefDto.java
│   │   └── UserResponseDto.java
│   ├── event/
│   │   ├── EventDetailResponseDto.java
│   │   ├── EventInputDto.java
│   │   ├── EventListResponseDto.java
│   │   ├── EventResponseDto.java
│   │   └── SessionForEventDto.java
│   ├── room/
│   │   ├── RoomInputDto.java
│   │   ├── RoomListResponseDto.java
│   │   └── RoomResponseDto.java
│   ├── session/
│   │   ├── EventRefDto.java
│   │   ├── RoomRefDto.java
│   │   ├── SessionInputDto.java
│   │   ├── SessionResponseDto.java
│   │   └── SpeakerRefDto.java
│   └── speaker/
│       ├── ExternalLinkDto.java
│       ├── SpeakerInputDto.java
│       └── SpeakerResponseDto.java
├── entity/
│   ├── BlacklistedIp.java
│   ├── Event.java
│   ├── ExternalLink.java
│   ├── Room.java
│   ├── Session.java
│   ├── User.java
│   └── enums/
│       └── Role.java
├── exception/
│   ├── BadRequestException.java
│   ├── ConflictException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── NotFoundException.java
│   ├── TooManyRequestException.java
│   ├── UnauthorizedException.java
│   └── UnprocessableEntityException.java
├── mapper/
│   ├── EventMapper.java
│   ├── ExternalLinkMapper.java
│   ├── RoomMapper.java
│   ├── SessionMapper.java
│   ├── SpeakerMapper.java
│   └── UserMapper.java
├── repository/
│   ├── BlacklistedIpRepository.java
│   ├── EventRepository.java
│   ├── ExternalLinkRepository.java
│   ├── RoomEventExistence.java        # interface projection
│   ├── RoomRepository.java
│   ├── SessionRepository.java
│   └── UserRepository.java
├── service/
│   ├── AuthService.java
│   ├── EventService.java
│   ├── RoomService.java
│   ├── SessionService.java
│   └── SpeakerService.java
└── validator/
    ├── DataValidator.java
    ├── EventValidator.java
    └── SessionValidator.java

src/test/java/com/techindna/eventsyncapi/
├── EventSyncApiApplicationTests.java
├── controller/
│   ├── auth/
│   │   └── AuthControllerTest.java
│   ├── events/
│   │   ├── DeleteEventControllerTest.java
│   │   ├── GetEventControllerTest.java
│   │   ├── PostEventControllerTest.java
│   │   └── PutEventControllerTest.java
│   ├── rooms/
│   │   ├── DeleteRoomControllerTest.java
│   │   ├── GetRoomByIdControllerTest.java
│   │   ├── GetRoomControllerTest.java
│   │   ├── PostRoomControllerTest.java
│   │   └── PutRoomControllerTest.java
│   ├── sessions/
│   │   └── PostSessionControllerTest.java
│   └── speakers/
│       └── PostSpeakerControllerTest.java
└── service/
    ├── auth/
    │   └── AuthServiceTest.java
    ├── events/
    │   ├── DeleteEventServiceTest.java
    │   ├── GetEventServiceTest.java
    │   ├── PostEventServiceTest.java
    │   └── PutEventServiceTest.java
    ├── rooms/
    │   ├── DeleteRoomServiceTest.java
    │   ├── GetRoomByIdServiceTest.java
    │   ├── GetRoomServiceTest.java
    │   ├── PostRoomServiceTest.java
    │   └── PutRoomServiceTest.java
    ├── sessions/
    │   └── PostSessionServiceTest.java
    └── speakers/
        └── PostSpeakerServiceTest.java

src/main/resources/
├── application.properties
└── db/
    ├── auth/
    │   ├── auth_data.sql
    │   ├── ip_blacklist_schema.sql
    │   └── users_schema.sql
    ├── events/
    │   ├── delete_event_data.sql
    │   ├── events_schema.sql
    │   ├── get_events_data.sql
    │   └── put_event_data.sql
    ├── externalLink/
    │   └── external_links_schema.sql
    ├── rooms/
    │   ├── delete_room_data.sql
    │   ├── get_room_by_id_data.sql
    │   ├── put_room_data.sql
    │   └── rooms_schema.sql
    └── sessions/
        ├── session_speaker_schema.sql
        ├── sessions_schema.sql
        └── test_session_data.sql

scripts/
├── auth/
│   └── test_post_auth_login.sh
├── event/
│   ├── test_delete_event.sh
│   ├── test_get_events.sh
│   ├── test_post_event.sh
│   └── test_put_event.sh
├── room/
│   ├── test_delete_room.sh
│   ├── test_get_room_by_id.sh
│   ├── test_post_room.sh
│   └── test_put_room.sh
├── sessions/
│   └── test_post_sessions.sh
└── speaker/
    └── test_post_speaker.sh

docs/
├── api.yaml              # OpenAPI 3.0.3 spec
├── mcd.canvas            # Obsidian canvas — conceptual data model
└── .obsidian/            # Obsidian vault config
```

## Common commands

```bash
./gradlew compileJava          # compile only (fast)
./gradlew test                 # run all tests (171 tests)
./gradlew bootRun              # start server → http://localhost:8080
./gradlew build -x test        # full build without tests
```

## Conventions

- **DDL** — `ddl-auto=validate`. Schema is managed externally via SQL scripts in `src/main/resources/db/`. Never use `update` or `create`.
- **Entities** — use `@Table(schema = "eventsync_app")`. Table names match the MCD (singular: `"user"`, `"room"`, `"blacklisted_ip"`, `"session"`, `"external_links"`).
- **Queries** — prefer `@Query` over JdbcTemplate. Use `@Modifying` + `RETURNING` for write queries. List columns explicitly, no `SELECT *`.
- **IDs** — UUID PKs generated with `GenerationType.UUID` (Hibernate 6+).
- **OpenAPI** — camelCase fields (`firstName`, `createdAt`), US English, 3.0.3. Every endpoint declares 400 and 422 explicitly.
- **Security** — JWT extracted from cookie `"jwt"`. Auth config lives in `config/` package. Filter clears context for bad JWT — no framework exceptions, `ExceptionTranslationFilter` + custom handlers return JSON 401/403.
- **Validation** — **All validation in the service layer via `DataValidator`** (and `SessionValidator` for sessions, `EventValidator` for events). DTOs are plain `@Data` beans with no `@NotBlank`/`@Valid` annotations. `DataValidator` handles null checks, format regex, name/email/URL validation, text length limits, and external link validation.
- **Exception handling** — business exceptions (`BadRequestException`, `UnprocessableEntityException`, etc.) thrown from services, caught by `GlobalExceptionHandler` (`@RestControllerAdvice`). Error response format: `{status, error, message}`.
- **Tests** — `@DisplayName` in English. Constructor injection with `mock()` (no `@Mock`, no `@ExtendWith`). Controller tests use `MockMvcBuilders.standaloneSetup` + `GlobalExceptionHandler` as controller advice. Service tests use Mockito only. Test subpackages per endpoint (e.g., `service/sessions/`, `controller/speakers/`).
- **IP blacklist** — `AuthService.checkBlacklist(ipAddress)` guards GET endpoints (events, rooms). Rate-limited to 5 failed login attempts per IP via `BlacklistedIp` entity.
- **Mappers** — Aggregate facade pattern: `SessionMapper` depends on `EventMapper`, `RoomMapper`, `SpeakerMapper`. `SpeakerMapper` depends on `ExternalLinkMapper`. Services depend only on the aggregate mapper, never on sub-mappers.
- **Event** — CRUD via `EventRepository` native queries with `RETURNING` (`insertEvent`, `updateEventById`, `deleteEventById`). Title is unique (`ON CONFLICT (title) DO NOTHING`). `EventValidator` validates fields and date ordering (endDate after startDate). `EventMapper` computes `isLive` and provides detail responses with nested sessions.
- **Session** — Created via `POST /sessions`. Uses `SessionValidator` for validation, `SessionRepository.findRoomAndEventExistence()` for DB existence check before insert. `SessionMapper` computes `isLive` (between startDate/endDate) and resolves speaker refs.
- **Speaker** — Created via `POST /speakers` (role `SPEAKER`). Supports nested `externalLinks` array saved via `ExternalLinkRepository.insertExternalLink()` per-row. Uses `UserRepository.insertSpeaker()` with `ON CONFLICT (email) DO NOTHING`.

## Common pitfalls

- `.env` is **gitignored**. Required vars: `PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `JWT_TOKEN`, `PGSSLMODE`, `CORS_ALLOWED_ORIGINS`.
- DB is Neon PostgreSQL pooler — connections may be transient. Use `channel_binding=require` + `sslmode=require`.
- OpenAPI spec is hand-written in `docs/api.yaml`, not generated.
- MCD is an Obsidian canvas (`docs/mcd.canvas`) — parse as JSON to read nodes/edges.
- `User` entity table name is `"user"` (reserved keyword, needs quotes in native queries).
- `Event` title is unique (DB constraint `ON CONFLICT (title)`).
- `Session` title is unique (DB constraint `ON CONFLICT (title)`).
- `ExternalLink` url is unique (DB constraint `ON CONFLICT (url)`).
