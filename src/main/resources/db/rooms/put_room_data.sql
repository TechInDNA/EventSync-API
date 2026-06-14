INSERT INTO eventsync_app."room" (id, name)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Conference Room A')
ON CONFLICT (name) DO NOTHING;
