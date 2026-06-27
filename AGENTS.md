# EventSync-API

## Stack

- **Language** — Java 21
- **Framework** — Spring Boot 4.0.6 (webmvc, data-jpa, security)
- **Database** — PostgreSQL (Neon), schema `eventsync_app`
- **Build** — Gradle 9.5.1 (wrapper: `./gradlew`)
- **Auth** — JWT (auth0/java-jwt 4.5.2), Argon2 password encoder
- **AI** — Spring AI MCP Server (webmvc, SSE transport). `@Tool` beans auto-discovered by `spring-ai-starter-mcp-server-webmvc` at `/mcp/sse`.
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
│   ├── AiConversationController.java
│   ├── AuthController.java
│   ├── EventController.java
│   ├── QuestionController.java       # GET/POST /sessions/{id}/questions
│   ├── RoomController.java
│   ├── SessionController.java
│   └── SpeakerController.java        # + speaker external-link endpoints
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
│   ├── question/
│   │   ├── QuestionListResponseDto.java
│   │   ├── QuestionRequestDto.java
│   │   ├── QuestionResponseDto.java
│   │   └── UpvoteResponseDto.java          # {upvoteCount} returned by toggle upvote
│   ├── room/
│   │   ├── RoomInputDto.java
│   │   ├── RoomListResponseDto.java
│   │   └── RoomResponseDto.java
│   ├── ai/
│   │   ├── AiConversationDetailResponseDto.java
│   │   ├── AiConversationListResponseDto.java
│   │   ├── AiConversationResponseDto.java
│   │   ├── ChatMessageInputDto.java
│   │   └── ChatMessageResponseDto.java
│   ├── session/
│   │   ├── EventRefDto.java
│   │   ├── RoomRefDto.java
│   │   ├── SessionDetailResponseDto.java
│   │   ├── SessionInputDto.java
│   │   ├── SessionListResponseDto.java
│   │   ├── SessionResponseDto.java
│   │   ├── SessionSpeakerInputDto.java     # startTime/endTime for session-speaker link
│   │   ├── SessionSpeakerTimeSlotDto.java  # id/startTime/endTime from session_speaker
│   │   ├── SessionUpdateInputDto.java
│   │   └── SpeakerRefDto.java
│   └── speaker/
│       ├── ExternalLinkDto.java
│       ├── SessionForSpeakerDto.java
│       ├── SpeakerDetailResponseDto.java
│       ├── SpeakerInputDto.java
│       ├── SpeakerListResponseDto.java
│       ├── SpeakerResponseDto.java
│       ├── SpeakerUpdateInputDto.java
│       └── SpeakerUpdateResponseDto.java
├── entity/
│   ├── AiConversation.java
│   ├── BlacklistedIp.java
│   ├── Event.java
│   ├── ExternalLink.java
│   ├── Question.java                # fk → session, user; @Formula upvoteCount
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
│   ├── AiConversationMapper.java
│   ├── ChatMessageMapper.java
│   ├── EventMapper.java
│   ├── ExternalLinkMapper.java
│   ├── QuestionMapper.java          # maps Question entities to DTOs
│   ├── RoomMapper.java
│   ├── SessionMapper.java
│   ├── SpeakerMapper.java
│   └── UserMapper.java
├── repository/
│   ├── AiConversationRepository.java
│   ├── BlacklistedIpRepository.java
│   ├── ChatMessageRepository.java
│   ├── EventRepository.java
│   ├── ExternalLinkRepository.java  # + insert, update, delete methods
│   ├── QuestionRepository.java      # native queries with pagination + upvote counts + toggle upvote
│   ├── RoomEventExistence.java      # interface projection
│   ├── RoomRepository.java
│   ├── SessionRepository.java
│   └── UserRepository.java          # + findSpeakersByNameContaining, countSpeakersByNameContaining
├── service/
│   ├── AiApiService.java
│   ├── AiConversationService.java
│   ├── AuthService.java
│   ├── EventService.java
│   ├── QuestionService.java         # paginated GET + POST for session questions
│   ├── RoomService.java
│   ├── SessionService.java
│   └── SpeakerService.java          # + external-link CRUD methods
├── mcp/
│   └── RoomMcpTools.java            # MCP @Tool beans — room CRUD for Hermes
├── validator/
    ├── AiConversationsValidator.java
    ├── DataValidator.java
    ├── EventValidator.java
    ├── ExternalLinkValidator.java
    ├── QuestionValidator.java       # validates QuestionRequestDto
    ├── SessionValidator.java
    └── SpeakerValidator.java        # + validateGet for speaker list

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
│   ├── question/
│   │   ├── GetQuestionControllerTest.java
│   │   └── PostUpvoteQuestionControllerTest.java
│   ├── rooms/
│   │   ├── DeleteRoomControllerTest.java
│   │   ├── GetRoomByIdControllerTest.java
│   │   ├── GetRoomControllerTest.java
│   │   ├── PostRoomControllerTest.java
│   │   └── PutRoomControllerTest.java
│   ├── sessions/
│   │   ├── DeleteSessionSpeakerControllerTest.java
│   │   ├── GetSessionSpeakerControllerTest.java
│   │   ├── GetSessionControllerTest.java
│   │   ├── GetSessionsControllerTest.java
│   │   ├── PostSessionControllerTest.java
│   │   ├── PostSessionSpeakerControllerTest.java
│   │   └── PutSessionControllerTest.java
│   └── speakers/
│       ├── DeleteSpeakerControllerTest.java
│       ├── DeleteSpeakerExternalLinkControllerTest.java
│       ├── GetSpeakerByIdControllerTest.java
│       ├── GetSpeakersControllerTest.java
│       ├── PostSpeakerControllerTest.java
│       ├── PutSpeakerControllerTest.java
│       └── PutSpeakerExternalLinkControllerTest.java
└── service/
    ├── auth/
    │   └── AuthServiceTest.java
    ├── events/
    │   ├── DeleteEventServiceTest.java
    │   ├── GetEventServiceTest.java
    │   ├── PostEventServiceTest.java
    │   └── PutEventServiceTest.java
    ├── question/
    │   ├── GetQuestionServiceTest.java
    │   └── PostUpvoteQuestionServiceTest.java
    ├── rooms/
    │   ├── DeleteRoomServiceTest.java
    │   ├── GetRoomByIdServiceTest.java
    │   ├── GetRoomServiceTest.java
    │   ├── PostRoomServiceTest.java
    │   └── PutRoomServiceTest.java
    ├── sessions/
    │   ├── DeleteSessionSpeakerServiceTest.java
    │   ├── GetSessionSpeakerServiceTest.java
    │   ├── GetSessionServiceTest.java
    │   ├── GetSessionsServiceTest.java
    │   ├── PostSessionServiceTest.java
    │   ├── PostSessionSpeakerServiceTest.java
    │   └── PutSessionServiceTest.java
    └── speakers/
        ├── DeleteSpeakerServiceTest.java
        ├── GetSpeakerByIdServiceTest.java
        ├── GetSpeakersServiceTest.java
        ├── PostSpeakerServiceTest.java
        ├── PutSpeakerServiceTest.java
        └── PutSpeakerExternalLinkServiceTest.java

src/main/resources/
├── application.properties
└── db/
    ├── ai_conversations/
    │   └── ai_conversations_schema.sql
    ├── auth/
    │   ├── auth_data.sql
    │   ├── ip_blacklist_schema.sql
    │   └── users_schema.sql
    ├── events/
    │   ├── delete_event_data.sql
    │   ├── events_schema.sql
    │   ├── get_event_by_id_data.sql
    │   ├── get_events_data.sql
    │   └── put_event_data.sql
    ├── externalLink/
    │   ├── delete_external_link_data.sql
    │   ├── external_links_schema.sql
    │   ├── post_external_link_data.sql
    │   └── put_speaker_external_link_data.sql
    ├── questions/
    │   ├── question_schema.sql
    │   ├── post_question_data.sql
    │   └── test_questions_data.sql
    ├── rooms/
    │   ├── delete_room_data.sql
    │   ├── get_room_by_id_data.sql
    │   ├── get_rooms_data.sql
    │   ├── put_room_data.sql
    │   └── rooms_schema.sql
    ├── sessions/
    │   ├── delete_session_data.sql
    │   ├── delete_session_speaker_data.sql
    │   ├── get_session_by_id_data.sql
    │   ├── get_session_speaker_data.sql
    │   ├── get_sessions_data.sql
    │   ├── post_session_speaker_data.sql
    │   ├── put_session_data.sql
    │   ├── put_session_speaker_data.sql
    │   ├── session_speaker_schema.sql
    │   ├── sessions_schema.sql
    │   └── test_session_data.sql
    ├── speaker/
    │   ├── delete_speaker_data.sql
    │   ├── get_speaker_by_id_data.sql
    │   ├── get_speakers.sql
    │   └── put_speaker_data.sql
    ├── upvote/
    │   ├── post_upvote_data.sql
    │   └── upvote_schema.sql

scripts/
├── auth/
│   └── test_post_auth_login.sh
├── event/
│   ├── test_delete_event.sh
│   ├── test_get_event_by_id.sh
│   ├── test_get_events.sh
│   ├── test_post_event.sh
│   └── test_put_event.sh
├── external-link/
│   ├── test_delete_external_link.sh
│   ├── test_post_external_link.sh
│   └── test_put_speaker_external_link.sh
├── questions/
│   ├── test_get_questions.sh
│   ├── test_post_questions.sh
│   └── test_post_upvote.sh
├── room/
│   ├── test_delete_room.sh
│   ├── test_get_room_by_id.sh
│   ├── test_get_rooms.sh
│   ├── test_post_room.sh
│   └── test_put_room.sh
├── sessions/
│   ├── test_delete_session.sh
│   ├── test_delete_session_speaker.sh
│   ├── test_get_session_by_id.sh
│   ├── test_get_session_speaker.sh
│   ├── test_get_sessions.sh
│   ├── test_post_session_speaker.sh
│   ├── test_post_sessions.sh
│   ├── test_put_session_speaker.sh
│   └── test_put_sessions.sh
└── speaker/
    ├── test_delete_speaker.sh
    ├── test_get_speaker_by_id.sh
    ├── test_get_speakers.sh
    ├── test_post_speaker.sh
    └── test_put_speaker.sh

docs/
├── api.yaml              # OpenAPI 3.0.3 spec
├── mcd.canvas            # Obsidian canvas — conceptual data model
└── .obsidian/            # Obsidian vault config
```

## Common commands

```bash
./gradlew compileJava          # compile only (fast)
./gradlew test                 # run all tests (3 pre-existing AuthServiceTest failures)
./gradlew bootRun              # start server → http://localhost:8080
./gradlew build -x test        # full build without tests
```

## Conventions

- **DDL** — `ddl-auto=validate`. Schema is managed externally via SQL scripts in `src/main/resources/db/`. Never use `update` or `create`.
- **Entities** — use `@Table(schema = "eventsync_app")`. Table names match the MCD (singular: `"user"`, `"room"`, `"blacklisted_ip"`, `"session"`, `"external_links"`, `"ai_conversation"`).
- **Queries** — prefer `@Query` over JdbcTemplate. Use `@Modifying` + `RETURNING` for write queries. List columns explicitly, no `SELECT *`.
- **IDs** — UUID PKs generated with `GenerationType.UUID` (Hibernate 6+).
- **OpenAPI** — camelCase fields (`firstName`, `createdAt`), US English, 3.0.3. Every endpoint declares 400 and 422 explicitly.
- **Security** — JWT extracted from cookie `"jwt"`. Auth config lives in `config/` package. Filter clears context for bad JWT — no framework exceptions, `ExceptionTranslationFilter` + custom handlers return JSON 401/403. Rules: GET `/sessions/{id}/questions` is `permitAll`, POST is `authenticated()` (any role can post). Speaker external-link endpoints follow the same pattern as the parent resource (POST/PUT/DELETE require ADMIN).
- **Validation** — **All validation in the service layer via `DataValidator`** (and `SessionValidator` for sessions, `EventValidator` for events, `SpeakerValidator` for speakers, `ExternalLinkValidator` for external links, `AiConversationsValidator` for AI conversations). DTOs are plain `@Data` beans with no `@NotBlank`/`@Valid` annotations. `DataValidator` handles null checks, format regex, name/email/URL validation, text length limits, and external link validation.
- **Exception handling** — business exceptions (`BadRequestException`, `UnprocessableEntityException`, etc.) thrown from services, caught by `GlobalExceptionHandler` (`@RestControllerAdvice`). Error response format: `{status, error, message}`.
- **Tests** — `@DisplayName` in English. Constructor injection with `mock()` (no `@Mock`, no `@ExtendWith`). Controller tests use `MockMvcBuilders.standaloneSetup` + `GlobalExceptionHandler` as controller advice. Service tests use Mockito only. Test subpackages per endpoint (e.g., `service/sessions/`, `controller/speakers/`).
- **IP blacklist** — `AuthService.checkBlacklist(ipAddress)` guards GET endpoints (events, rooms, speakers). Rate-limited to 5 failed login attempts per IP via `BlacklistedIp` entity.
- **Mappers** — Aggregate facade pattern: `SessionMapper` depends on `EventMapper`, `RoomMapper`, `SpeakerMapper`. `SpeakerMapper` depends on `ExternalLinkMapper`. Services depend only on the aggregate mapper, never on sub-mappers.
- **Event** — Full CRUD via `EventRepository` native queries with `RETURNING` (`insertEvent`, `updateEventById`, `deleteEventById`). Title is unique (`ON CONFLICT (title) DO NOTHING`). `EventValidator` validates fields and date ordering (endDate after startDate). `EventMapper` computes `isLive` and provides detail responses with nested sessions. `GET /events/{id}` uses `findEventWithSessionsById()` with `LEFT JOIN FETCH` for eagerly loaded sessions (includes room). List endpoint supports filters: `title`, `location`, `startDate`, `endDate`, `isLive`.
- **Room** — Full CRUD via `RoomRepository` native queries with `RETURNING`. Name is unique (`ON CONFLICT (name) DO NOTHING`). `RoomValidator` validates the name. `GET /rooms/{id}` fetches by UUID. `RoomMapper` maps entity to `RoomResponseDto` (id, name).
- **Session** — Full CRUD via `SessionRepository` native queries with `RETURNING`. Title is unique (`ON CONFLICT (title) DO NOTHING`). `POST /sessions` requires `SessionInputDto` (all mandatory). `PUT /sessions/{id}` requires `SessionUpdateInputDto` (all mandatory — title, description, startDate, endDate, roomId, capacity, eventId). `DELETE /sessions/{id}` removes session and cascade-deletes `session_speaker` rows. Uses `SessionValidator.validateUpdate()` for PUT and `SessionValidator.validate()` for POST. `SessionMapper` computes `isLive` (between startDate/endDate) and resolves speaker refs from the join table. `SessionRepository.findRoomAndEventExistence()` performs a dual existence check before insert/update.
- **Session speaker sub-resource** — Four endpoints under `/sessions/{sessionId}/speaker/{speakerId}`: POST (add speaker with time slot), GET (list time slots for a speaker in a session), PUT (update time slot by `linkId` query param), DELETE (remove speaker from session). `session_speaker` PK is a standalone `id` (gen_random_uuid) — no `UNIQUE` on `(session_id, speaker_id)`, so a speaker can occupy multiple time slots in the same session. Overlapping time-slot validation via `SessionRepository.existsOverlappingSpeakerInRoom()` and `existsOverlappingSpeakerInRoomExcluding()`. PUT/POST require ADMIN; GET is public with IP blacklist check.
- **Speaker** — Full CRUD via `UserRepository`. Creation (`POST /speakers`) uses `insertSpeaker()` with `ON CONFLICT (email) DO NOTHING` and `ExternalLinkRepository.insertExternalLink()` per-row for nested links. Update (`PUT /speakers/{id}`) uses `updateSpeakerById()` with `RETURNING`, guarded by `SpeakerValidator.validateUpdate()` which allows optional `bio`/`profilePicture`. Detail (`GET /speakers/{id}`) fetches external links eagerly via `findByIdWithExternalLinks()` and resolves speaker sessions via `SessionRepository.findBySpeakerId()`. Deletion (`DELETE /speakers/{id}`) uses `deleteSpeakerById()` with `ON DELETE CASCADE` on external links. List (`GET /speakers`) filters by name search via `findSpeakersByNameContaining()`/`countSpeakersByNameContaining()`, eagerly loading external links via `findByUserIdIn()`. `SpeakerValidator` handles creation vs update validation separately (`validateCreation()` requires all fields, `validateUpdate()` allows optional bio/picture). `SpeakerValidator.validateGet()` validates the search string.
- **AI Conversation** — Created via `POST /ai/conversations` (requires `ROLE_ADMIN`). Uses `AiConversationsValidator.validateUserRequest()` to strip and validate the message. `AiApiService` wraps a Spring AI `ChatClient` (OpenAI-compatible API, configurable via `base-url` and `model`) with room MCP tools injected as tool context, enabling the AI to perform room operations via natural language. `AiConversationRepository.insertConversation()` persists the exchange with an auto-generated title and `@CreationTimestamp`. Response includes `id`, `title`, `userRequest`, `aiResponse`, `userId`, `createdAt`.
- **Question** — Sub-resource under `/sessions/{id}/questions`. GET is public (`permitAll`), POST requires any authenticated role (`authenticated()`). Uses `QuestionRepository` native queries with `LEFT JOIN upvote` for counts and `CASE WHEN :sort = 'upvotes' THEN COUNT(up.id) END DESC, CASE WHEN :sort = 'createdAt' THEN q.created_at END ASC` ordering. `QuestionMapper` hides the participant ref when `anonymous` is true. `QuestionValidator.validateUpdate()` validates title (string) and content (text). `insertQuestion()` uses `INSERT ... WHERE EXISTS (SELECT 1 FROM session WHERE id = :sessionId)` with `RETURNING` to enforce FK existence in a single query. `Question` entity has a `@Formula` for `upvoteCount` (computed via `SELECT COUNT(*) FROM upvote`). `QuestionResponseDto` includes an `upvotes` field.
- **Upvote** — Toggle endpoint `POST /sessions/{id}/questions/{qid}/upvote` (any authenticated role). Implements a toggle: attempts DELETE first, if nothing was deleted it INSERTs (this is a toggle, not a separate upvote/unupvote pair). `UNIQUE(user_id, question_id)` on the upvote table prevents duplicates. Returns `UpvoteResponseDto` with the current `upvoteCount`. `QuestionRepository` exposes `insertUpvote()`, `deleteUpvote()`, and `countUpvotesByQuestionId()`. The `Question` entity also has a `@Formula upvoteCount` for eager reads (e.g., session detail page).
- **Speaker External Links** — Managed as a sub-resource under `/speakers/{id}/external-link`. POST adds a link (returns all links for the speaker), PUT updates by `urlName` query param, DELETE removes by `externalLinkId` query param. All require ADMIN. `ExternalLinkRepository` provides `insertExternalLink()` (with `ON CONFLICT (url) DO NOTHING`), `updateExternalLinkByNameAndUserId()`, and `deleteExternalLinkByIdAndUserId()`. Each returns the current list of links for the speaker. `ExternalLinkValidator.validateSingleLink()` validates individual link DTOs. `SpeakerService` handles the try/catch for unique constraint violations (SQLState `23505`) on update.
- **MCP tools** — Room CRUD exposed as `@Tool` methods in `mcp/` package. Each tool wraps the existing service layer (validation included). `@ToolParam(description = ...)` is mandatory so the AI knows what to pass. Tools return user-friendly strings with success/error messages. All `/mcp/**` paths are **permitAll** in `SecurityConfig` (SSE transport is not JWT-authenticated).

## Common pitfalls

- `.env` is **gitignored**. Required vars: `PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `JWT_SECRET`, `PGSSLMODE`, `CORS_ALLOWED_ORIGINS`, `AI_API_KEY`, `AI_MODEL`.
- DB is Neon PostgreSQL pooler — connections may be transient. Use `channel_binding=require` + `sslmode=require`.
- OpenAPI spec is hand-written in `docs/api.yaml`, not generated.
- MCD is an Obsidian canvas (`docs/mcd.canvas`) — parse as JSON to read nodes/edges.
- `User` entity table name is `"user"` (reserved keyword, needs quotes in native queries).
- `Event` title is unique (DB constraint `ON CONFLICT (title)`).
- `Session` title is unique (DB constraint `ON CONFLICT (title)`).
- `ExternalLink` url is unique (DB constraint `ON CONFLICT (url)`).
- `Room` name is unique (DB constraint `ON CONFLICT (name)`).
- Application config uses `JWT_SECRET` as the env var, not `JWT_TOKEN`. The example `.env` must use `JWT_SECRET`.
- 3 pre-existing `AuthServiceTest` failures (null-check order in `AuthService.registerParticipant`) are unrelated to other endpoints.
- `QuestionRepository` queries use `CAST(:sort AS text)` inside `CASE WHEN` — sorting works for `upvotes` and `createdAt` only. Other sort values fall through to the default `q.created_at DESC` ordering.
- `ExternalLinkRepository.updateExternalLinkByNameAndUserId()` identifies links by **name** string, not by ID — the `urlName` request param in the PUT endpoint matches the old link name before update. The uniqueness constraint is on `url`, not name.
- `ExternalLinkValidator` has two modes: `externalLinkValidator()` (validates a list of links for speaker creation) and `validateSingleLink()` (validates a single link for add/update endpoints).
