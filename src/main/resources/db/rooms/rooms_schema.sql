
CREATE TABLE IF NOT EXISTS eventsync_app."room" (
    id   UUID        DEFAULT gen_random_uuid()  NOT NULL  PRIMARY KEY,
    name VARCHAR(50)                            NOT NULL  UNIQUE
);
