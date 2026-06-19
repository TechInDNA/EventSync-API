
INSERT INTO eventsync_app."room" (id, name)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Salle Principale')
ON CONFLICT (name) DO NOTHING;

-- Seed: event
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901',
     'Session Test Event',
     'Event de test pour les sessions.',
     '2026-07-01 09:00:00+03', '2026-07-03 18:00:00+03', 'Antananarivo')
ON CONFLICT (title) DO NOTHING;

-- Seed: session (used by POST /sessions/{id}/questions)
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
