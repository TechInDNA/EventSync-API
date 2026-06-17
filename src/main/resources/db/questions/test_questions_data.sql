-- =============================================================================
-- Mock data: questions for testing GET /sessions/{id}/questions
-- Dependencies: run auth_data.sql + test_session_data.sql first
-- UUIDs generated via uuidgen
-- =============================================================================

-- Seed: participant users (no password — participants don't login)
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, role, created_at)
VALUES
    ('ebcfde24-726e-4bb4-8b55-786781d8b6bc', 'Alice',  'Dupont',  'alice@example.com',   'PARTICIPANT', NOW()),
    ('7372fc18-5a3f-45bc-b1c0-cd612c18f26f', 'Bob',    'Martin',  'bob@example.com',     'PARTICIPANT', NOW()),
    ('b5718099-653c-4ed5-b8f1-da808ff2dc7e', 'Charlie','Bernard', 'charlie@example.com', 'PARTICIPANT', NOW()),
    ('e869cfc5-49cf-4788-a6f3-e746f84427d0', 'Diana',  'Petit',   'diana@example.com',   'PARTICIPANT', NOW()),
    ('171f0762-3aa4-4015-a060-7ea69f229978', 'Eve',    'Moreau',  'eve@example.com',     'PARTICIPANT', NOW())
ON CONFLICT (email) DO NOTHING;

-- Seed: session attached to existing room + event from test_session_data.sql
INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id, created_at)
VALUES (
    '4089df22-c66d-4c9c-bfa9-8bcec8d2e943',
    'Introduction to Spring Boot',
    'A beginner-friendly session on Spring Boot fundamentals.',
    '2026-07-01 10:00:00+03',
    '2026-07-01 12:00:00+03',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    50,
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    NOW()
)
ON CONFLICT (title) DO NOTHING;

-- Seed: questions — mix of anonymous = false and anonymous = true
INSERT INTO eventsync_app.question (id, title, content, session_id, user_id, anonymous, created_at)
VALUES
    -- Anonymous = false (identified participants)
    ('ab5c8e49-340e-4775-8dd4-a72b5c78fa4c', 'Dependency Injection?',
     'How does Spring resolve circular dependencies?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'ebcfde24-726e-4bb4-8b55-786781d8b6bc', false,
     '2026-07-01 10:05:00+03'),

    ('d763be3f-30c4-4327-9e02-2866102a5d5f', 'Best practices for DI',
     'What are the recommended patterns for constructor injection vs field injection?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'ebcfde24-726e-4bb4-8b55-786781d8b6bc', false,
     '2026-07-01 10:08:00+03'),

    ('4d63c985-696a-4958-ae64-2dd8e835ee2c', 'Spring vs Spring Boot',
     'What is the main difference between Spring Framework and Spring Boot?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', '7372fc18-5a3f-45bc-b1c0-cd612c18f26f', false,
     '2026-07-01 10:12:00+03'),

    ('643ad140-24fb-44db-96b3-3fd9d20153e4', 'Security configuration',
     'How do I configure method-level security with @PreAuthorize?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'e869cfc5-49cf-4788-a6f3-e746f84427d0', false,
     '2026-07-01 10:20:00+03'),

    -- Anonymous = true (hidden identity)
    ('3c70b28a-6952-4deb-ac7b-85a7b63448e4', 'Database connection pool',
     'What connection pool does Spring Boot use by default?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'b5718099-653c-4ed5-b8f1-da808ff2dc7e', true,
     '2026-07-01 10:15:00+03'),

    ('9ecf917a-6e36-4cf0-8be1-4230e9435883', 'Handling transactions',
     'How do I handle nested transactions across multiple services?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', '171f0762-3aa4-4015-a060-7ea69f229978', true,
     '2026-07-01 10:25:00+03'),

    ('75f61cf2-05b0-4bd9-9a9f-9c7689530e13', 'Performance tuning',
     'Any tips for reducing Spring Boot startup time?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'ebcfde24-726e-4bb4-8b55-786781d8b6bc', true,
     '2026-07-01 10:30:00+03'),

    ('36325442-1eea-4ac8-9908-2670d75f96fc', 'Testing strategies',
     'What is the difference between @MockBean and @WebMvcTest?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', '7372fc18-5a3f-45bc-b1c0-cd612c18f26f', true,
     '2026-07-01 10:35:00+03'),

    ('17573ebb-454c-4781-99aa-ba5be8437e04', 'Async methods',
     'When should I use @Async vs CompletableFuture directly?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'e869cfc5-49cf-4788-a6f3-e746f84427d0', true,
     '2026-07-01 10:40:00+03'),

    ('008583e7-e1e6-4add-9e46-7348b267ca15', 'REST API versioning',
     'How do you version REST APIs in a Spring Boot application?',
     '4089df22-c66d-4c9c-bfa9-8bcec8d2e943', 'b5718099-653c-4ed5-b8f1-da808ff2dc7e', true,
     '2026-07-01 10:45:00+03')
ON CONFLICT (id) DO NOTHING;

-- Seed: upvotes on questions
INSERT INTO eventsync_app.upvote (id, user_id, question_id, created_at)
VALUES
    -- "Dependency Injection?" — 3 upvotes
    ('1c752bda-e6bd-4093-8742-0fb034067474', 'ebcfde24-726e-4bb4-8b55-786781d8b6bc', 'ab5c8e49-340e-4775-8dd4-a72b5c78fa4c', '2026-07-01 10:06:00+03'),
    ('04829323-76a9-4838-838f-6de5218c7a46', '7372fc18-5a3f-45bc-b1c0-cd612c18f26f', 'ab5c8e49-340e-4775-8dd4-a72b5c78fa4c', '2026-07-01 10:07:00+03'),
    ('a28661b1-645d-49b2-bced-afb25da78ebb', 'b5718099-653c-4ed5-b8f1-da808ff2dc7e', 'ab5c8e49-340e-4775-8dd4-a72b5c78fa4c', '2026-07-01 10:09:00+03'),

    -- "Best practices for DI" — 2 upvotes
    ('7430e8eb-a05a-4c92-89cb-62d5906b4614', 'e869cfc5-49cf-4788-a6f3-e746f84427d0', 'd763be3f-30c4-4327-9e02-2866102a5d5f', '2026-07-01 10:10:00+03'),
    ('86af31d3-e807-4cf5-8a52-6bc2df48a57a', '171f0762-3aa4-4015-a060-7ea69f229978', 'd763be3f-30c4-4327-9e02-2866102a5d5f', '2026-07-01 10:11:00+03'),

    -- "Testing strategies" — 1 upvote
    ('4a1d4475-e62f-4cd4-9982-61c4290d06ad', '171f0762-3aa4-4015-a060-7ea69f229978', '36325442-1eea-4ac8-9908-2670d75f96fc', '2026-07-01 10:36:00+03')
ON CONFLICT (id) DO NOTHING;
