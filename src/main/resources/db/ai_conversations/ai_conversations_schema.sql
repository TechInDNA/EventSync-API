
CREATE TABLE IF NOT EXISTS eventsync_app.ai_conversation (
    id           UUID                                       DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    title        VARCHAR(100)                                                         NOT NULL,
    user_request TEXT                                                                 NOT NULL,
    ai_response  TEXT                                                                 NOT NULL,
    user_id      UUID                                                                 NOT NULL REFERENCES eventsync_app."user"(id) ON DELETE CASCADE,
    created_at   TIMESTAMP WITH TIME ZONE                   DEFAULT CURRENT_TIMESTAMP NOT NULL
);
