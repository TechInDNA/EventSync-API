CREATE TABLE IF NOT EXISTS eventsync_app."upvote" (
    id              UUID          NOT NULL   PRIMARY KEY,
    user_id         UUID  NOT NULL REFERENCES eventsync_app.user(id) ON DELETE CASCADE,
    question_id     UUID     NOT NULL REFERENCES eventsync_app.question ON DELETE CASCADE,
    created_at      timestamp     DEFAULT now()
);