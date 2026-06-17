CREATE TABLE IF NOT EXISTS eventsync_app."upvote" (
    id              UUID          NOT NULL   PRIMARY KEY,
    user_id         UUID  NOT NULL REFERENCES eventsync_app.users(id) ON DELETE SET NULL,
    question_id     UUID     NOT NULL REFERENCES eventsync_app.question ON DELETE SET NULL,
    created_at      timestamp     DEFAULT now(),

);