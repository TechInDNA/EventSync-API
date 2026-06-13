
CREATE TABLE IF NOT EXISTS eventsync_app."session" (
    id          UUID        DEFAULT gen_random_uuid()  NOT NULL  PRIMARY KEY,
    title       VARCHAR(50)                            NOT NULL  UNIQUE,
    description TEXT                                   NOT NULL,
    start_date  TIMESTAMP WITH TIME ZONE               NOT NULL,
    end_date    TIMESTAMP WITH TIME ZONE               NOT NULL,
    room_id     UUID                                             REFERENCES eventsync_app.room(id) ON DELETE SET NULL,
    capacity    INT                                    NOT NULL,
    event_id    UUID                                   NOT NULL  REFERENCES eventsync_app.event(id) ON DELETE CASCADE,
    created_at  TIMESTAMP WITH TIME ZONE  DEFAULT now()  NOT NULL
);
