-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing GET /sessions
--  Tests: pagination, live filter, room/speaker/event search
--
--  Data summary:
--    • 1 room                        – "Session Room Alpha"
--    • 1 event                       – "Tech Summit 2026"
--    • 3 speakers                    – Alice, Bob, Carol
--    • 6 sessions (2 live, 4 not live)
--      - Live #1 : 2 speakers
--      - Live #2 : 1 speaker
--      - Not live (past)   #3 : 1 speaker
--      - Not live (past)   #4 : no speaker
--      - Not live (future) #5 : 2 speakers
--      - Not live (future) #6 : no speaker
-- ═══════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────
--  Room
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.room (id, name)
VALUES ('aaaaaaaa-1111-1111-1111-111111111111', 'Session Room Alpha')
ON CONFLICT (name) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Event
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'bbbbbbbb-2222-2222-2222-222222222222',
    'Tech Summit 2026',
    'Conférence de test pour les endpoints de sessions.',
    '2025-06-01 09:00:00+03',
    '2027-12-31 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Speakers
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'cccccccc-3333-3333-3333-333333333333',
    'Alice',
    'Wonder',
    'alice.wonder@getsessions.example.com',
    'Cloud infrastructure expert and K8s enthusiast.',
    'https://example.com/avatars/alice.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'dddddddd-4444-4444-4444-444444444444',
    'Bob',
    'Builder',
    'bob.builder@getsessions.example.com',
    'DevOps and automation specialist.',
    NULL,
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'eeeeeeee-5555-5555-5555-555555555555',
    'Carol',
    'Code',
    'carol.code@getsessions.example.com',
    'Full-stack developer and Spring Boot advocate.',
    'https://example.com/avatars/carol.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #1 — LIVE (wide date range), with 2 speakers
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f1f1f1f1-1111-1111-1111-111111111111',
    'Kubernetes in Production',
    'Déploiement et gestion de clusters K8s à grande échelle.',
    '2025-01-01 00:00:00+00',
    '2027-12-31 23:59:59+00',
    'aaaaaaaa-1111-1111-1111-111111111111',
    80,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES
    ('f1f1f1f1-1111-1111-1111-111111111111', 'cccccccc-3333-3333-3333-333333333333'),
    ('f1f1f1f1-1111-1111-1111-111111111111', 'dddddddd-4444-4444-4444-444444444444')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #2 — LIVE (wide date range), with 1 speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f2f2f2f2-2222-2222-2222-222222222222',
    'Spring Boot 4 Deep Dive',
    'Virtual threads, JWT, et patterns d''architecture moderne.',
    '2025-06-01 00:00:00+00',
    '2027-12-31 23:59:59+00',
    'aaaaaaaa-1111-1111-1111-111111111111',
    100,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('f2f2f2f2-2222-2222-2222-222222222222', 'eeeeeeee-5555-5555-5555-555555555555')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #3 — NOT LIVE (past), with 1 speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f3f3f3f3-3333-3333-3333-333333333333',
    'Docker Fundamentals',
    'Containers, images, et Docker Compose.',
    '2023-01-10 09:00:00+03',
    '2023-01-10 17:00:00+03',
    'aaaaaaaa-1111-1111-1111-111111111111',
    50,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('f3f3f3f3-3333-3333-3333-333333333333', 'cccccccc-3333-3333-3333-333333333333')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #4 — NOT LIVE (past), no speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f4f4f4f4-4444-4444-4444-444444444444',
    'Git Best Practices',
    'Stratégies de branching, rebase vs merge, et CI/CD.',
    '2024-03-15 10:00:00+03',
    '2024-03-15 12:00:00+03',
    'aaaaaaaa-1111-1111-1111-111111111111',
    30,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #5 — NOT LIVE (future), with 2 speakers
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f5f5f5f5-5555-5555-5555-555555555555',
    'AI-Powered Development',
    'LLMs, génération de code, et workflows assistés par IA.',
    '2028-01-15 09:00:00+03',
    '2028-01-15 17:00:00+03',
    'aaaaaaaa-1111-1111-1111-111111111111',
    120,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES
    ('f5f5f5f5-5555-5555-5555-555555555555', 'dddddddd-4444-4444-4444-444444444444'),
    ('f5f5f5f5-5555-5555-5555-555555555555', 'eeeeeeee-5555-5555-5555-555555555555')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #6 — NOT LIVE (future), no speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f6f6f6f6-6666-6666-6666-666666666666',
    'Quantum Computing Intro',
    'Qubits, portes quantiques, et algorithmes.',
    '2029-06-01 09:00:00+03',
    '2029-06-03 18:00:00+03',
    'aaaaaaaa-1111-1111-1111-111111111111',
    40,
    'bbbbbbbb-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;
