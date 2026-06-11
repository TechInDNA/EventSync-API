
CREATE TABLE IF NOT EXISTS eventsync_app."event" (
    id          UUID        DEFAULT gen_random_uuid()  NOT NULL  PRIMARY KEY,
    title       VARCHAR(50)                            NOT NULL  UNIQUE,
    description TEXT                                   NOT NULL,
    start_date  TIMESTAMP                              NOT NULL,
    end_date    TIMESTAMP                              NOT NULL,
    location    VARCHAR(50)                            NOT NULL,
    created_at  TIMESTAMP   DEFAULT now()              NOT NULL
);
