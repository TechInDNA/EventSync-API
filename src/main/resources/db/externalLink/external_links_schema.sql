
CREATE TABLE IF NOT EXISTS eventsync_app.external_links (
    id         UUID           DEFAULT gen_random_uuid()  NOT NULL    PRIMARY KEY,
    name       VARCHAR(50)    NOT NULL,
    url        VARCHAR(255)   NOT NULL    UNIQUE,
    user_id    UUID           NOT NULL    REFERENCES eventsync_app."user"(id) ON DELETE CASCADE
);
