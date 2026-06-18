-- Seed: speakers for DELETE /speakers/{id}/external-link testing
-- Self-contained script (no external dependencies).

-- ============================================================
-- Speaker A: with 3 external links — to test targeted deletion
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '95f04aa3-431f-453a-8067-137a0e2fd718',
    'Multi',
    'Link',
    'multi.link@example.com',
    'Speaker with multiple external links for delete testing.',
    'https://example.com/avatars/multi.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO UPDATE
    SET id = EXCLUDED.id;

INSERT INTO eventsync_app.external_links (id, name, url, user_id)
VALUES
    ('e1722b17-ecc6-4c03-b5b2-74b6b15d4fe1', 'LinkedIn',  'https://linkedin.com/in/multi-link',    '95f04aa3-431f-453a-8067-137a0e2fd718'),
    ('73b5c723-5cbd-4a29-aad3-20d54cba6615', 'Twitter',   'https://twitter.com/multi_link',        '95f04aa3-431f-453a-8067-137a0e2fd718'),
    ('0c68d440-3f20-46f7-87dc-1fe802ed9dbd', 'Website',   'https://multi-link.example.com',        '95f04aa3-431f-453a-8067-137a0e2fd718')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker B: no external links
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'f94b5d76-9a2d-47cf-8caf-399a384f7f79',
    'No',
    'Link',
    'no.link@example.com',
    'Speaker without any external links.',
    'https://example.com/avatars/none.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO UPDATE
    SET id = EXCLUDED.id;
