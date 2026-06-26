-- =============================================================================
-- Mock data: session detail for testing GET /sessions/{id}
-- Self-contained: creates its own room, event, speakers, questions, upvotes.
-- UUIDs generated via uuidgen.
-- =============================================================================

-- ───────────────────────────────────────────────────────────────────────────
-- Room
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."room" (id, name)
VALUES (
    '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
    'Salle GetSessionById'
)
ON CONFLICT (name) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Event
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
    'GetSessionById Event',
    'Event used to test GET /sessions/{id}.',
    '2026-08-01 09:00:00+03',
    '2026-08-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Speakers (two users)
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES
    ('5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b', 'Sophie', 'Martin',
     'sophie.martin.get-session@example.com',
     'Senior backend engineer passionate about Spring Boot.', NULL, 'SPEAKER'),
    ('6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c', 'Lucas', 'Dubois',
     'lucas.dubois.get-session@example.com',
     'Realtime systems specialist and conference speaker.', NULL, 'SPEAKER')
ON CONFLICT (email) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Session #1 — has BOTH speakers AND questions (the canonical case)
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
    'GET Session By ID - Full Detail',
    'Session with speakers and questions for testing GET /sessions/{id}.',
    '2026-08-01 10:00:00+03',
    '2026-08-01 12:00:00+03',
    '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
    80,
    '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id, start_time, end_time)
VALUES
    ('7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', '5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b', '2026-08-01T10:00:00+03:00', '2026-08-01T10:55:00+03:00'),
    ('7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', '6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c', '2026-08-01T11:00:00+03:00', '2026-08-01T12:00:00+03:00')
ON CONFLICT (session_id, speaker_id) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Session #2 — has speakers but NO questions (questions array null)
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e',
    'GET Session By ID - Speakers Only',
    'Session with speakers but no questions for testing GET /sessions/{id}.',
    '2026-08-01 14:00:00+03',
    '2026-08-01 16:00:00+03',
    '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
    40,
    '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id, start_time, end_time)
VALUES (
    '8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e',
    '5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
    '2026-08-01T14:00:00+03:00',
    '2026-08-01T16:00:00+03:00'
)
ON CONFLICT (session_id, speaker_id) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Session #3 — no speakers, no questions (sparse case)
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f',
    'GET Session By ID - Empty',
    'Empty session for testing GET /sessions/{id}.',
    '2026-08-02 09:00:00+03',
    '2026-08-02 11:00:00+03',
    '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
    20,
    '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Participants (for question askers)
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, role, created_at)
VALUES
    ('a0b1c2d3-e4f5-6789-0abc-def123456789', 'Camille', 'Roux',
     'camille.roux.get-session@example.com', 'PARTICIPANT', NOW()),
    ('b1c2d3e4-f5a6-7890-1bcd-ef2345678901', 'Hugo',    'Leroy',
     'hugo.leroy.get-session@example.com', 'PARTICIPANT', NOW()),
    ('c2d3e4f5-a6b7-8901-2cde-f34567890123', 'Emma',    'Garcia',
     'emma.garcia.get-session@example.com', 'PARTICIPANT', NOW())
ON CONFLICT (email) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Questions on Session #1 — mix of identified and anonymous
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.question (id, title, content, session_id, user_id, anonymous, created_at)
VALUES
    -- Identified question with upvotes
    ('d3e4f5a6-b7c8-9012-3def-456789012345', 'How to bootstrap a Spring Boot app?',
     'Walk me through @SpringBootApplication.',
     '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'a0b1c2d3-e4f5-6789-0abc-def123456789', false,
     '2026-08-01 10:05:00+03'),

    -- Identified question, no upvotes
    ('e4f5a6b7-c8d9-0123-4ef0-567890123456', 'Best practices for REST controllers?',
     'Any recommendations on response shaping?',
     '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'b1c2d3e4-f5a6-7890-1bcd-ef2345678901', false,
     '2026-08-01 10:10:00+03'),

    -- Anonymous question with upvotes
    ('f5a6b7c8-d9e0-1234-5f01-678901234567', 'Connection pool tuning?',
     'What pool size do you recommend for prod?',
     '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'c2d3e4f5-a6b7-8901-2cde-f34567890123', true,
     '2026-08-01 10:15:00+03'),

    -- Anonymous question, no upvotes
    ('a6b7c8d9-e0f1-2345-6012-789012345678', 'Logging best practices?',
     'How do you structure logs in a Spring Boot project?',
     '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'a0b1c2d3-e4f5-6789-0abc-def123456789', true,
     '2026-08-01 10:20:00+03')
ON CONFLICT (id) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────────
-- Upvotes on Session #1's questions
-- ───────────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.upvote (id, user_id, question_id, created_at)
VALUES
    -- "How to bootstrap a Spring Boot app?" — 3 upvotes
    ('1a2b3c4d-5e6f-7081-92a3-b4c5d6e7f801', 'a0b1c2d3-e4f5-6789-0abc-def123456789',
     'd3e4f5a6-b7c8-9012-3def-456789012345', '2026-08-01 10:06:00+03'),
    ('2b3c4d5e-6f70-8192-a3b4-c5d6e7f80102', 'b1c2d3e4-f5a6-7890-1bcd-ef2345678901',
     'd3e4f5a6-b7c8-9012-3def-456789012345', '2026-08-01 10:07:00+03'),
    ('3c4d5e6f-7081-92a3-b4c5-d6e7f8010203', 'c2d3e4f5-a6b7-8901-2cde-f34567890123',
     'd3e4f5a6-b7c8-9012-3def-456789012345', '2026-08-01 10:08:00+03'),

    -- "Connection pool tuning?" — 1 upvote
    ('4d5e6f70-8192-a3b4-c5d6-e7f801020304', 'b1c2d3e4-f5a6-7890-1bcd-ef2345678901',
     'f5a6b7c8-d9e0-1234-5f01-678901234567', '2026-08-01 10:16:00+03')
ON CONFLICT (id) DO NOTHING;
