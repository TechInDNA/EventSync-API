INSERT INTO eventsync_app."room" (id, name)
VALUES ('134873ba-e426-4e1b-8943-a8ee4d0951b7', 'Room de test GET by ID')
ON CONFLICT (name) DO NOTHING;
