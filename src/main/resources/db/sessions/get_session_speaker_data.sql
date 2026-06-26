-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing GET /sessions/{sessionId}/speaker/{speakerId}
--  Creates a room, event, session, speaker, and session_speaker link.
--  UUIDs generated via uuidgen.
-- ═══════════════════════════════════════════════════════════════════════

INSERT INTO eventsync_app.room (id, name)
VALUES ('00107bc0-8e75-406e-b534-c1d0f17a4803', 'GetSessionSpeakerRoom')
ON CONFLICT (name) DO NOTHING;

INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '4fba8de1-8f8b-4f19-9c67-34a278bd8d9e',
    'GetSessionSpeakerEvent',
    'Event for GET /sessions/{id}/speaker/{id}.',
    '2026-08-01 09:00:00+03',
    '2026-08-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '4795f35f-e4d9-4ee3-a7f2-c420ae445bbb',
    'GetSessionSpeakerSession',
    'Session for GET /sessions/{id}/speaker/{id}.',
    '2026-08-02 09:00:00+03',
    '2026-08-02 17:00:00+03',
    '00107bc0-8e75-406e-b534-c1d0f17a4803',
    100,
    '4fba8de1-8f8b-4f19-9c67-34a278bd8d9e'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'c9d22036-5e44-4f70-9199-13a73cc1119f',
    'GetSpeaker',
    'SlotTester',
    'getspeaker.slottester@example.com',
    'Speaker for GET /sessions/{id}/speaker/{id}.',
    'https://example.com/getspeaker.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (id, session_id, speaker_id, start_time, end_time)
VALUES (
    'b82b907a-2074-4b49-9d9e-79ecd3abfdeb',
    '4795f35f-e4d9-4ee3-a7f2-c420ae445bbb',
    'c9d22036-5e44-4f70-9199-13a73cc1119f',
    '2026-08-02 14:00:00+03',
    '2026-08-02 15:30:00+03'
)
ON CONFLICT (id) DO NOTHING;
