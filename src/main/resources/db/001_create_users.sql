
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

-- Seed: admin user (password: admin123)
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, password, role, created_at)
VALUES (
    '3f553f56-792c-4c80-9ea9-b259ef1247a9',
    'Admin',
    'User',
    'admin@eventsync.com',
    '$argon2id$v=19$m=16384,t=2,p=1$dizuhrMYICNif5ZLchCBrw$FfApSuvcBfHNWUbyVG/HXItFbpVS0EpErUxQdNIrldg',
    'ADMIN',
    NOW()
) ON CONFLICT (email) DO NOTHING;
