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
VALUES ('95c89274-3a9e-4ecd-8084-c33072a9a698', 'Session Room Alpha')
ON CONFLICT (name) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────────
--  Event
-- ───────────────────────────────────────────────────────────────────────
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '0fe49568-881a-406c-8657-5cb3b1d971f7',
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
    'edb0d25a-e420-4b07-8411-5ab1c15b8f5c',
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
    'be05d2a7-ee16-486d-ae45-93625dd19b03',
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
    '4f359ffa-5099-4651-8024-6a0033bd2e1e',
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
    '376cd180-7309-48a3-ae28-92fc0715dee4',
    'Kubernetes in Production',
    'Déploiement et gestion de clusters K8s à grande échelle.',
    '2025-01-01 00:00:00+00',
    '2027-12-31 23:59:59+00',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    80,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES
    ('376cd180-7309-48a3-ae28-92fc0715dee4', 'edb0d25a-e420-4b07-8411-5ab1c15b8f5c'),
    ('376cd180-7309-48a3-ae28-92fc0715dee4', 'be05d2a7-ee16-486d-ae45-93625dd19b03')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #2 — LIVE (wide date range), with 1 speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '0e1c042c-3e8a-485b-9f73-28d4ae2ac273',
    'Spring Boot 4 Deep Dive',
    'Virtual threads, JWT, et patterns d''architecture moderne.',
    '2025-06-01 00:00:00+00',
    '2027-12-31 23:59:59+00',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    100,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('0e1c042c-3e8a-485b-9f73-28d4ae2ac273', '4f359ffa-5099-4651-8024-6a0033bd2e1e')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #3 — NOT LIVE (past), with 1 speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '4f79e056-e929-4f00-bcc3-ebb01412198b',
    'Docker Fundamentals',
    'Containers, images, et Docker Compose.',
    '2023-01-10 09:00:00+03',
    '2023-01-10 17:00:00+03',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    50,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('4f79e056-e929-4f00-bcc3-ebb01412198b', 'edb0d25a-e420-4b07-8411-5ab1c15b8f5c')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #4 — NOT LIVE (past), no speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'a26f07ce-fd82-42ab-a9a2-8460d9f79007',
    'Git Best Practices',
    'Stratégies de branching, rebase vs merge, et CI/CD.',
    '2024-03-15 10:00:00+03',
    '2024-03-15 12:00:00+03',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    30,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #5 — NOT LIVE (future), with 2 speakers
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'd1d818cc-ba33-4eea-95f8-92b487d1a4b3',
    'AI-Powered Development',
    'LLMs, génération de code, et workflows assistés par IA.',
    '2028-01-15 09:00:00+03',
    '2028-01-15 17:00:00+03',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    120,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES
    ('d1d818cc-ba33-4eea-95f8-92b487d1a4b3', 'be05d2a7-ee16-486d-ae45-93625dd19b03'),
    ('d1d818cc-ba33-4eea-95f8-92b487d1a4b3', '4f359ffa-5099-4651-8024-6a0033bd2e1e')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  SESSION #6 — NOT LIVE (future), no speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '3c6f8187-85ca-42dd-b35a-0bcc5305c001',
    'Quantum Computing Intro',
    'Qubits, portes quantiques, et algorithmes.',
    '2029-06-01 09:00:00+03',
    '2029-06-03 18:00:00+03',
    '95c89274-3a9e-4ecd-8084-c33072a9a698',
    40,
    '0fe49568-881a-406c-8657-5cb3b1d971f7'
)
ON CONFLICT (title) DO NOTHING;
