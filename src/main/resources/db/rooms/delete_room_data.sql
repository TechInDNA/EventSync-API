-- Seed: room à supprimer (UUID fixe pour les tests de suppression)
INSERT INTO eventsync_app."room" (id, name)
VALUES ('a1111111-1111-4111-8111-111111111111', 'Room à supprimer')
ON CONFLICT (name) DO NOTHING;
