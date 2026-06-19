-- Seed: events + sessions for testing GET /events/{id}
-- Event with 3 sessions
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '2b2ad0e8-e8bd-475d-9737-86a1e4081f44',
    'DevCon 2026',
    'Conférence développeurs — architecture, cloud, et bonnes pratiques.',
    '2026-08-10 09:00:00+03',
    '2026-08-12 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES
    (
        '21aaabab-ac74-441a-ad83-60fc16f9dfbc',
        'Clean Architecture en Spring Boot',
        'Principes hexagonal architecture et implémentation avec Spring Boot.',
        '2026-08-10 10:00:00+03', '2026-08-10 12:00:00+03',
        'cfca8633-9500-4d3d-822d-ef59a4d7b03e', 100,
        '2b2ad0e8-e8bd-475d-9737-86a1e4081f44'
    ),
    (
        '042db648-8d29-4cfc-80b1-c48823bb6e32',
        'Kubernetes pour les Développeurs',
        'Déploiement, scaling, et debugging d''applications sur K8s.',
        '2026-08-11 14:00:00+03', '2026-08-11 17:00:00+03',
        'e36ec732-d4f9-470b-bc66-31323afe15bf', 60,
        '2b2ad0e8-e8bd-475d-9737-86a1e4081f44'
    ),
    (
        '8cc091e7-105c-4f24-8dd1-305ea30bfe47',
        'Workshop React Native',
        'Développement mobile cross-platform avec React Native et Expo.',
        '2026-08-12 09:00:00+03', '2026-08-12 16:00:00+03',
        '149745a5-8045-4aaa-9a46-c77fe3941295', 30,
        '2b2ad0e8-e8bd-475d-9737-86a1e4081f44'
    )
ON CONFLICT (title) DO NOTHING;

-- Event with no sessions
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'd4ef0aa8-76d2-464b-8e2c-5c0cd3b3621f',
    'Standalone Expo 2026',
    'Salon exposition sans sessions programmées.',
    '2026-09-20 08:00:00+03',
    '2026-09-22 20:00:00+03',
    'Toamasina'
)
ON CONFLICT (title) DO NOTHING;
