-- Seed: speakers for GET /speakers/{id} testing
-- Self-contained script (no external dependencies).

-- ============================================================
-- Speaker 1: no external links, no sessions
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'c0c59fa0-be24-4994-bc6c-884dbb1953d0',
    'Alice',
    'Solo',
    'alice.solo@example.com',
    'Speaker isolé, sans sessions.',
    'https://example.com/avatars/alice.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- Speaker 2: with external links and sessions
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'c0c59fa0-be24-4994-bc6c-884dbb1953d1',
    'Bob',
    'Linké',
    'bob.linke@example.com',
    'Speaker avec sessions et liens externes.',
    'https://example.com/avatars/bob.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- External links for speaker 2
INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES
    ('Twitter',  'https://twitter.com/bob',  'c0c59fa0-be24-4994-bc6c-884dbb1953d1'),
    ('GitHub',   'https://github.com/bob',   'c0c59fa0-be24-4994-bc6c-884dbb1953d1'),
    ('LinkedIn', 'https://linkedin.com/in/bob', 'c0c59fa0-be24-4994-bc6c-884dbb1953d1')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Dependencies for the session (room + event)
-- ============================================================
INSERT INTO eventsync_app."room" (id, name)
VALUES ('c0c59fa0-be24-4994-bc6c-884dbb1953f0', 'Salle Speaker Test')
ON CONFLICT (name) DO NOTHING;

INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'c0c59fa0-be24-4994-bc6c-884dbb1953f1',
    'Speaker Test Event',
    'Événement pour tester les speakers.',
    '2026-07-01 09:00:00+03',
    '2026-07-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ============================================================
-- Session
-- ============================================================
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'c0c59fa0-be24-4994-bc6c-884dbb1953e0',
    'Keynote de Bob',
    'Présentation principale par Bob sur les microservices.',
    '2026-07-02 10:00:00+03',
    '2026-07-02 12:00:00+03',
    'c0c59fa0-be24-4994-bc6c-884dbb1953f0',
    100,
    'c0c59fa0-be24-4994-bc6c-884dbb1953f1'
)
ON CONFLICT (title) DO NOTHING;

-- Link speaker 2 to the session
INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('c0c59fa0-be24-4994-bc6c-884dbb1953e0', 'c0c59fa0-be24-4994-bc6c-884dbb1953d1')
ON CONFLICT DO NOTHING;
