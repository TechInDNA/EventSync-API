-- Seed: speakers for PUT /speakers/{id}/external-link testing
-- Self-contained script (no external dependencies).
--
-- Usage:
--   UPDATE eventsync_app.external_links SET name='…', url='…'
--   WHERE name='Twitter' AND user_id='b8a86aa8-5174-4843-b247-f2e09ccd3573';

-- ============================================================
-- Speaker 1: with 2 external links — update one by urlName
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'b8a86aa8-5174-4843-b247-f2e09ccd3573',
    'Linked',
    'Speaker',
    'linked.speaker.put@example.com',
    'Speaker with external links to update via PUT.',
    'https://example.com/avatars/linked-put.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (id, name, url, user_id)
VALUES
    ('8be5f531-422d-45be-8cd4-2c0f1ee6a401', 'Twitter',  'https://twitter.com/linked_put',  'b8a86aa8-5174-4843-b247-f2e09ccd3573'),
    ('955e683f-4083-456d-a264-edfbf5e093a0', 'GitHub',   'https://github.com/linked_put',   'b8a86aa8-5174-4843-b247-f2e09ccd3573')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker 2: no external links — test 404 on urlName match
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '965840a0-bf4e-46ae-a6b3-076e75f376dc',
    'Solo',
    'Speaker',
    'solo.speaker.put@example.com',
    'Speaker with no external links.',
    'https://example.com/avatars/solo-put.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;
