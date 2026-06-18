-- Seed: speakers for DELETE /speakers/{id}/external-link testing
-- Self-contained script (no external dependencies).

-- ============================================================
-- Speaker A: with 3 external links — to test targeted deletion
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'd4e5f6a7-b8c9-0123-4567-89abcdef0123',
    'Multi',
    'Link',
    'multi.link@example.com',
    'Speaker with multiple external links for delete testing.',
    'https://example.com/avatars/multi.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES
    ('LinkedIn',  'https://linkedin.com/in/multi-link',    'd4e5f6a7-b8c9-0123-4567-89abcdef0123'),
    ('Twitter',   'https://twitter.com/multi_link',        'd4e5f6a7-b8c9-0123-4567-89abcdef0123'),
    ('Website',   'https://multi-link.example.com',        'd4e5f6a7-b8c9-0123-4567-89abcdef0123')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker B: no external links
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'e5f6a7b8-c9d0-1234-5678-9abcdef01234',
    'No',
    'Link',
    'no.link@example.com',
    'Speaker without any external links.',
    'https://example.com/avatars/none.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;
