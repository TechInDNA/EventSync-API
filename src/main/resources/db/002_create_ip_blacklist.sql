
CREATE TABLE IF NOT EXISTS eventsync_app.ip_blacklist (
    id               UUID         NOT NULL    PRIMARY KEY,
    ip_address       VARCHAR(20)  NOT NULL    UNIQUE,
    user_agent       VARCHAR(255),
    failed_attempts  INT          NOT NULL    DEFAULT 1,
    created_at       TIMESTAMP    NOT NULL    DEFAULT NOW()
);
