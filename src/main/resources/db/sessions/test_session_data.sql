-- Seed: rooms for session testing
INSERT INTO eventsync_app."room" (id, name)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Salle Principale')
ON CONFLICT (name) DO NOTHING;

-- Seed: events for session testing
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901',
     'Session Test Event',
     'Event de test pour les sessions.',
     '2026-07-01 09:00:00+03', '2026-07-03 18:00:00+03', 'Antananarivo')
ON CONFLICT (title) DO NOTHING;
