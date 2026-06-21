
CREATE TABLE IF NOT EXISTS eventsync_app.conversation (
    id         UUID                           DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    title      VARCHAR(100)                                                            NOT NULL,
    user_id    UUID                                                                     NOT NULL REFERENCES eventsync_app."user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE        DEFAULT CURRENT_TIMESTAMP                NOT NULL
);

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sender_type' AND typnamespace = 'eventsync_app'::regnamespace) THEN
            CREATE TYPE eventsync_app.sender_type AS ENUM ('user', 'agent');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS eventsync_app.chat_message (
    id              UUID                           DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    content         TEXT                                                            NOT NULL,
    sender_type     eventsync_app.sender_type                                      NOT NULL,
    conversation_id UUID                                                            NOT NULL REFERENCES eventsync_app.conversation(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE        DEFAULT CURRENT_TIMESTAMP        NOT NULL
);
