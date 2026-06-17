-- Seed: speakers for POST /speakers/{id}/external-link testing
-- Self-contained script (no external dependencies).

-- ============================================================
-- Speaker 1: with 2 existing external links
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'a34db822-3568-42e5-b94c-62eb45a996af',
    'Jean',
    'Lié',
    'jean.lie@example.com',
    'Speaker avec déjà des liens externes.',
    'https://example.com/avatars/jean.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES
    ('Twitter',  'https://twitter.com/jean_lie',   'a34db822-3568-42e5-b94c-62eb45a996af'),
    ('GitHub',   'https://github.com/jean_lie',    'a34db822-3568-42e5-b94c-62eb45a996af')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker 2: no external links
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '6d2dfa8c-25bc-4a3c-9ba1-a316e7a3c5a0',
    'Solo',
    'Nu',
    'solo.nu@example.com',
    'Speaker sans aucun lien externe.',
    'https://example.com/avatars/solo.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;
