

CREATE TABLE IF NOT EXISTS eventsync_app."question" (
    id              UUID          NOT NULL   PRIMARY KEY,
    title           VARCHAR(50)   NOT NULL,
    content         text          NOT NULL ,
    created_at      timestamp     DEFAULT now(),
    session_id      uuid          NOT NULL    REFERENCES eventsync_app.session(id) ON DELETE CASCADE,
    user_id         uuid          NOT NULL    REFERENCES eventsync_app.user(id)    ON DELETE SET NULL,
    anonymous       boolean       DEFAULT false
);