-- Seed: sample speakers for GET /speakers listing
-- Self-contained script (no external dependencies).
--
-- Speakers 1-2: have external links (multi-link and single-link)
-- Speakers 3-5: no external links
--
-- Usage:
--   psql -f src/main/resources/db/speaker/seed_speakers.sql
--

-- ============================================================
-- Speaker 1: with 2 external links
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '80b2c6b1-e767-4fc8-bff5-a82541001551',
    'Ada',
    'Lovelace',
    'ada.lovelace@example.com',
    'First computer programmer and visionary mathematician.',
    'https://example.com/avatars/ada.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES
    ('Twitter',  'https://twitter.com/ada_lovelace',  '80b2c6b1-e767-4fc8-bff5-a82541001551'),
    ('GitHub',   'https://github.com/ada_lovelace',   '80b2c6b1-e767-4fc8-bff5-a82541001551')
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker 2: with 1 external link
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '3a062c00-6cad-485f-9e0c-a15f271027d5',
    'Grace',
    'Hopper',
    'grace.hopper@example.com',
    'Rear Admiral and pioneer of compiler programming.',
    NULL,
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO eventsync_app.external_links (name, url, user_id)
VALUES (
    'LinkedIn',
    'https://linkedin.com/in/grace-hopper',
    '3a062c00-6cad-485f-9e0c-a15f271027d5'
)
ON CONFLICT (url) DO NOTHING;

-- ============================================================
-- Speaker 3: no external links, with profile picture
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'dcb8e6ec-d751-4ee1-ad96-811ee5ecd674',
    'Alan',
    'Turing',
    'alan.turing@example.com',
    'Founding father of theoretical computer science and AI.',
    'https://example.com/avatars/turing.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- Speaker 4: no external links, no profile picture
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    '39984705-7441-423c-ae28-29a418a3f441',
    'Katherine',
    'Johnson',
    'katherine.johnson@example.com',
    'NASA mathematician whose calculations enabled Apollo missions.',
    NULL,
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- Speaker 5: no external links, no bio
-- ============================================================
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'ea74ca34-70ff-434a-a150-dbdef7ac34fa',
    'Margaret',
    'Hamilton',
    'margaret.hamilton@example.com',
    NULL,
    'https://example.com/avatars/hamilton.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;
