
CREATE TABLE IF NOT EXISTS eventsync_app.session_speaker (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    session_id  UUID          NOT NULL  REFERENCES eventsync_app."session"(id) ON DELETE CASCADE,
    speaker_id  UUID          NOT NULL  REFERENCES eventsync_app."user"(id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ   NOT NULL,
    end_time    TIMESTAMPTZ   NOT NULL,
    UNIQUE (start_time, end_time)
);
