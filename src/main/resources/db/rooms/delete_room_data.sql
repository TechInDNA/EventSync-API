-- Seed: room à supprimer (UUID fixe pour les tests de suppression)
INSERT INTO eventsync_app."room" (id, name)
VALUES ('37731480-3994-4ed0-8ee6-9b4f7811ea84', 'Room à supprimer')
ON CONFLICT (name) DO NOTHING;
