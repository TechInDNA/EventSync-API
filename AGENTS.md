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
docs/
├── api.yaml              # OpenAPI 3.0.3 spec (~1700 lines)
├── mcd.canvas            # Obsidian canvas — conceptual data model
└── .obsidian/            # Obsidian vault config
src/
├── main/
│   ├── java/com/techindna/eventsyncapi/
│   │   ├── EventSyncApiApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── TokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── entity/
│   │       ├── User.java
│   │       └── enums/
│   │           └── Role.java
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── 001_create_users.sql
└── test/
    └── java/.../EventSyncApiApplicationTests.java
build.gradle
settings.gradle
```

## Common commands

```bash
./gradlew compileJava          # compile only (fast)
./gradlew test                 # run tests
./gradlew bootRun              # start server → http://localhost:8080
./gradlew build -x test        # full build without tests
```

## Conventions

- **DDL** — `ddl-auto=validate`. Schema is managed externally via SQL scripts in `src/main/resources/db/`. Never use `update` or `create`.
- **Entities** — use `@Table(schema = "eventsync_app")`. Table names match the MCD (singular: `"user"`, etc.).
- **Queries** — prefer `@Query` over JdbcTemplate. Use `@Modifying` + `RETURNING` for write queries. List columns explicitly, no `SELECT *`.
- **IDs** — UUID PKs generated with `GenerationType.UUID` (Hibernate 6+).
- **OpenAPI** — camelCase fields (`firstName`, `createdAt`), US English, 3.0.3. Every endpoint declares 400 and 422 explicitly.
- **Security** — JWT extracted from cookie `"jwt"`. Auth config lives in `config/` package.
- **Tests** — `@DisplayName` in English. Service tests use Mockito only (no MockMvc). Naming: `Get{X}ServiceTest`.

## Common pitfalls

- `.env` is **gitignored**. Required vars: `PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `JWT_TOKEN`, `PGSSLMODE`.
- DB is Neon PostgreSQL pooler — connections may be transient. Use `channel_binding=require` + `sslmode=require`.
- OpenAPI spec is hand-written in `docs/api.yaml`, not generated.
- MCD is an Obsidian canvas (`docs/mcd.canvas`) — parse as JSON to read nodes/edges.
