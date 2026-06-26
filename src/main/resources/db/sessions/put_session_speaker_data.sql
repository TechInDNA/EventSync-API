-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing PUT /sessions/{sessionId}/speaker/{speakerId}
--
--  Scenarios:
--    1. Single-link speaker — linked to exactly one session
--       (update has no impact on other links)
--    2. Multi-link speaker — linked to two sessions
--       (updating one link leaves the other intact)
-- ═══════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────
--  Room
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.room (id, name)
VALUES ('21ecd3c8-e01f-4639-9d76-ca8d6dcc72c0', 'Session Speaker Test Room')
ON CONFLICT (name) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Event
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '9919dcb4-6166-48d4-82c4-3acd10f7191e',
    'Put Session-Speaker Test Event',
    'Événement de test pour PUT /sessions/{sessionId}/speaker/{speakerId}.',
    '2026-07-01 09:00:00+03',
    '2026-07-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Speaker 1 — linked to a single session
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'cfec79b8-b71e-40ec-a340-4ceb26a2acbb',
    'Alice',
    'SoloSpeaker',
    'alice.solo.speaker@example.com',
    'Speaker linked to exactly one session — single-link scenario.',
    'https://example.com/avatars/alice-solo.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Speaker 2 — linked to two sessions
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '6b74ea7b-bb2b-4e91-ba70-d2f037772083',
    'Bob',
    'MultiSpeaker',
    'bob.multi.speaker@example.com',
    'Speaker linked to two sessions — multi-link scenario.',
    'https://example.com/avatars/bob-multi.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session 1 — single speaker (Alice)
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'a0a3f4a0-a694-42cb-85dd-edc611f5b059',
    'PUT Test - Solo Speaker Session',
    'Session avec un seul orateur (Alice).',
    '2026-07-02 09:00:00+03',
    '2026-07-02 11:00:00+03',
    '21ecd3c8-e01f-4639-9d76-ca8d6dcc72c0',
    50,
    '9919dcb4-6166-48d4-82c4-3acd10f7191e'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session 2 — first session shared by Bob
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'dd99b878-b2db-4805-b607-4b903426d527',
    'PUT Test - Multi Session A',
    'Première session de Bob (multi-link scenario).',
    '2026-07-02 11:30:00+03',
    '2026-07-02 13:30:00+03',
    '21ecd3c8-e01f-4639-9d76-ca8d6dcc72c0',
    50,
    '9919dcb4-6166-48d4-82c4-3acd10f7191e'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session 3 — second session shared by Bob
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '3432c3e5-786b-4748-830c-c033fc986c79',
    'PUT Test - Multi Session B',
    'Deuxième session de Bob (multi-link scenario).',
    '2026-07-02 14:00:00+03',
    '2026-07-02 16:00:00+03',
    '21ecd3c8-e01f-4639-9d76-ca8d6dcc72c0',
    50,
    '9919dcb4-6166-48d4-82c4-3acd10f7191e'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session-Speaker links
-- ───────────────────────────────────────────────────────────────────────

-- Link 1: Alice → Session 1 (single-link scenario)
INSERT INTO eventsync_app.session_speaker (id, session_id, speaker_id, start_time, end_time)
VALUES (
    '743f3dbd-3706-45b6-adea-f48936aa815e',
    'a0a3f4a0-a694-42cb-85dd-edc611f5b059',
    'cfec79b8-b71e-40ec-a340-4ceb26a2acbb',
    '2026-07-02T09:00:00+03:00',
    '2026-07-02T11:00:00+03:00'
)
ON CONFLICT DO NOTHING;

-- Link 2: Bob → Session 2 (multi-link scenario, first link)
INSERT INTO eventsync_app.session_speaker (id, session_id, speaker_id, start_time, end_time)
VALUES (
    '22b6fe56-5224-40d1-91e3-07804c156a51',
    'dd99b878-b2db-4805-b607-4b903426d527',
    '6b74ea7b-bb2b-4e91-ba70-d2f037772083',
    '2026-07-02T11:30:00+03:00',
    '2026-07-02T13:00:00+03:00'
)
ON CONFLICT DO NOTHING;

-- Link 3: Bob → Session 3 (multi-link scenario, second link)
INSERT INTO eventsync_app.session_speaker (id, session_id, speaker_id, start_time, end_time)
VALUES (
    'f4745c33-5018-4f5e-84c4-bdc1d943b604',
    '3432c3e5-786b-4748-830c-c033fc986c79',
    '6b74ea7b-bb2b-4e91-ba70-d2f037772083',
    '2026-07-02T14:00:00+03:00',
    '2026-07-02T15:30:00+03:00'
)
ON CONFLICT DO NOTHING;
