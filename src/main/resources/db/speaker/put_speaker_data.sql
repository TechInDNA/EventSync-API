-- Seed: speaker for PUT /speakers/{id} testing
-- Self-contained script (no external dependencies).

INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'c0c59fa0-be24-4994-bc6c-884dbb1953a0',
    'Original',
    'Speaker',
    'original.speaker@example.com',
    'Speaker to be updated via PUT.',
    'https://example.com/avatars/original.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;
