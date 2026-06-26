-- =============================================================================
-- Seed data for testing POST /sessions/{id}/questions/{qid}/upvote
-- Dependencies: run upvote_schema.sql first
-- UUIDs generated via uuidgen
-- =============================================================================

-- Seed: room
INSERT INTO eventsync_app."room" (id, name)
VALUES
    ('6dee2131-c9b0-4e8f-94a1-02dcf059880f', 'Salle Upvote')
ON CONFLICT (name) DO NOTHING;

-- Seed: event
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES
    ('a9d84681-e726-4620-b1c8-255f410f6f8c',
     'Upvote Test Event',
     'Event de test pour le upvote.',
     '2026-07-01 09:00:00+03', '2026-07-03 18:00:00+03', 'Antananarivo')
ON CONFLICT (title) DO NOTHING;

-- Seed: session
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id, created_at)
VALUES (
    'ded92053-a92a-4979-b7be-4330e555861c',
    'Upvote Test Session',
    'Session de test pour le upvote.',
    '2026-07-01 10:00:00+03',
    '2026-07-01 12:00:00+03',
    '6dee2131-c9b0-4e8f-94a1-02dcf059880f',
    50,
    'a9d84681-e726-4620-b1c8-255f410f6f8c',
    NOW()
)
ON CONFLICT (title) DO NOTHING;

-- Seed: test user (participant) for upvote testing
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, role, created_at)
VALUES
    ('5b492d89-918c-42e6-9f7b-703343a1910e', 'Upvote', 'Tester', 'upvote.tester@example.com', 'PARTICIPANT', NOW())
ON CONFLICT (email) DO NOTHING;

-- Seed: question to upvote
INSERT INTO eventsync_app.question (id, title, content, session_id, user_id, anonymous, created_at)
VALUES (
    '3a81ea5b-2acc-4ff0-a277-e9c5c2d724d3',
    'Upvote Test Question',
    'This question is used for testing the POST upvote endpoint.',
    'ded92053-a92a-4979-b7be-4330e555861c',
    '5b492d89-918c-42e6-9f7b-703343a1910e',
    false,
    '2026-07-01 10:05:00+03'
)
ON CONFLICT (id) DO NOTHING;
