
CREATE TABLE IF NOT EXISTS eventsync_app.conversation (
    id         UUID                           DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    title      VARCHAR(100)                                                            NOT NULL,
    user_id    UUID                                                                     NOT NULL REFERENCES eventsync_app."user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE        DEFAULT CURRENT_TIMESTAMP                NOT NULL
);

CREATE TABLE IF NOT EXISTS eventsync_app.message (
    id              UUID                           DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    content         TEXT                                                            NOT NULL,
    sender_type     VARCHAR(10)                                                     NOT NULL,
    conversation_id UUID                                                            NOT NULL REFERENCES eventsync_app.conversation(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE        DEFAULT CURRENT_TIMESTAMP        NOT NULL
);
