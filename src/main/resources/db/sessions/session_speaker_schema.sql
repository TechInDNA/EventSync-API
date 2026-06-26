
CREATE TABLE IF NOT EXISTS eventsync_app.session_speaker (
    session_id  UUID                    NOT NULL  REFERENCES eventsync_app."session"(id) ON DELETE CASCADE,
    speaker_id  UUID                    NOT NULL  REFERENCES eventsync_app."user"(id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ  NOT NULL,
    end_time    TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (session_id, speaker_id),
    UNIQUE (start_time, end_time)
);
