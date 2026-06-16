

CREATE TABLE IF NOT EXISTS eventsync_app."question" (
    id              UUID          NOT NULL   PRIMARY KEY,
    title           VARCHAR(255)  NOT NULL,
    content         text          NOT NULL ,
    created_at      timestamp     DEFAULT now(),
    session_id      uuid          NOT NULL    REFERENCES eventsync_app.sessions(id) ON DELETE CASCADE,
    user_id         uuid          NOT NULL    REFERENCES eventsync_app.users(id)    ON DELETE CASCADE,
    anonymous       boolean       DEFAULT false
);