-- ═══════════════════════════════════════════════════════════════════════
--  Seed: session data for PUT /sessions/{id} testing
--  Self-contained: creates its own room, event, speaker, and sessions.
-- ═══════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────
--  Room
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."room" (id, name)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Salle Principale'
)
ON CONFLICT (name) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Event
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Session Test Event',
    'Événement de test pour les sessions.',
    '2026-07-01 09:00:00+03',
    '2026-07-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Speaker user — linked to one of the sessions
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'd1e2f3a4-b5c6-7d8e-9f0a-b1c2d3e4f5a0',
    'Jean',
    'Speaker',
    'jean.put-session-speaker@example.com',
    'Speaker for PUT /sessions/{id} testing.',
    'https://example.com/avatars/jean.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session #1 — has a speaker linked
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'e1f2a3b4-c5d6-7e8f-9a0b-c1d2e3f4a5b0',
    'PUT Test - Session With Speaker',
    'Session ayant un orateur lié, utilisée pour tester PUT /sessions/{id}.',
    '2026-07-02 09:00:00+03',
    '2026-07-02 12:00:00+03',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    100,
    'b2c3d4e5-f6a7-8901-bcde-f12345678901'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES (
    'e1f2a3b4-c5d6-7e8f-9a0b-c1d2e3f4a5b0',
    'd1e2f3a4-b5c6-7d8e-9f0a-b1c2d3e4f5a0'
)
ON CONFLICT (session_id, speaker_id) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Session #2 — no speaker linked
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f1a2b3c4-d5e6-7f8a-9b0c-d1e2f3a4b5c0',
    'PUT Test - Session Without Speaker',
    'Session sans orateur, utilisée pour tester PUT /sessions/{id}.',
    '2026-07-02 14:00:00+03',
    '2026-07-02 16:00:00+03',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    75,
    'b2c3d4e5-f6a7-8901-bcde-f12345678901'
)
ON CONFLICT (title) DO NOTHING;
