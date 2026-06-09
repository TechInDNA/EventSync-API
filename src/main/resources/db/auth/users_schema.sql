
CREATE SCHEMA IF NOT EXISTS eventsync_app;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE eventsync_app.user_role AS ENUM ('ADMIN', 'SPEAKER', 'PARTICIPANT');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS eventsync_app."user" (
    id              UUID                             NOT NULL    PRIMARY KEY,
    first_name      VARCHAR(50)                      NOT NULL,
    last_name       VARCHAR(50)                      NOT NULL,
    email           VARCHAR(50)                      NOT NULL    UNIQUE,
    password        VARCHAR(100),
    bio             TEXT,
    profile_picture VARCHAR(255),
    role            eventsync_app.user_role,
    created_at      TIMESTAMP                        NOT NULL    DEFAULT NOW()
);


