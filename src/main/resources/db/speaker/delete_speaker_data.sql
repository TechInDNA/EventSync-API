-- Seed: speaker for testing DELETE /speakers/{id}
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'f1f2f3f4-f5f6-7890-abcd-ef1234567890',
    'Delete',
    'Me',
    'delete.me@example.com',
    'Speaker to be deleted.',
    'https://example.com/avatars/delete-me.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- Speaker 2: with 2 external links — to test cascade delete
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'f1f2f3f4-f5f6-7890-abcd-ef1234567891',
    'Linked',
    'Speaker',
    'linked.speaker@example.com',
    'Speaker with external links to cascade-delete.',
    'https://example.com/avatars/linked.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES
    ('Twitter',  'https://twitter.com/linked_speaker',  'f1f2f3f4-f5f6-7890-abcd-ef1234567891'),
    ('GitHub',   'https://github.com/linked_speaker',   'f1f2f3f4-f5f6-7890-abcd-ef1234567891')
ON CONFLICT (url) DO NOTHING;
