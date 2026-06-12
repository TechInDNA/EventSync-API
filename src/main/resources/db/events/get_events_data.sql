-- Seed: sample events for testing filters
-- UUIDs generated via uuidgen
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES
    -- === 2025 — passés ===
    ('5ce88592-6f7f-42b1-8b0a-b5136d6abbca',
     'Conférence Java 2025',
     'Java et Spring Boot — virtual threads, performance, et architecture moderne.',
     '2025-08-15 09:00:00+02', '2025-08-17 18:00:00+02', 'Antananarivo'),

    ('ceb42a12-431e-4e3c-a538-1f2c375b1e62',
     'Hackathon Cybersec',
     'CTF, ateliers sécurité offensive et défensive, et conférences.',
     '2025-09-01 08:00:00+03', '2025-09-02 20:00:00+03', 'Fianarantsoa'),

    ('82b3ec85-f4f2-4379-9963-2b9dc7fc6c82',
     'Workshop PostgreSQL',
     'Performance, tuning, indexation, et requêtes avancées.',
     '2025-07-10 10:00:00+03', '2025-07-10 16:00:00+03', 'Toamasina'),

    ('3831fbec-cce8-46c6-b49a-d8114896c283',
     'DevOps Meetup',
     'CI/CD, Docker, Kubernetes, et Infrastructure as Code.',
     '2025-11-05 14:00:00+03', '2025-11-05 17:00:00+03', 'Antananarivo'),

    ('c6c72c10-5033-4718-b07e-2d91b0d8ca62',
     'AI Summer School',
     'Deep learning, NLP, et vision par ordinateur — 5 jours intensifs.',
     '2025-10-01 09:00:00+03', '2025-10-05 17:00:00+03', 'Mahajanga'),

    ('22e4790b-4c15-400d-9b13-c380593f789b',
     'React Native Workshop',
     'Développement mobile cross-platform avec React Native et Expo.',
     '2025-06-15 09:00:00+03', '2025-06-15 17:00:00+03', 'Antananarivo'),

    ('f530c13c-1aff-4bb7-8021-17cd82f5a22b',
     'Kubernetes Hands-on',
     'Déploiement, scaling, et monitoring de clusters K8s.',
     '2025-04-20 10:00:00+03', '2025-04-20 18:00:00+03', 'Fianarantsoa'),

    ('05be7b6b-4925-43a8-8b24-e354c1cb46b1',
     'API Design & GraphQL',
     'Bonnes pratiques REST, GraphQL, et documentation avec OpenAPI.',
     '2025-05-10 08:00:00+03', '2025-05-10 16:00:00+03', 'Antananarivo'),

    ('112d6044-5a4a-4445-975c-e9e3834ceb5e',
     'Cloud Native Day',
     'AWS, Azure, GCP — comparaison et migration cloud.',
     '2025-03-22 09:00:00+03', '2025-03-22 17:00:00+03', 'Toamasina'),

    ('98534dc1-d673-4b03-9ee8-cc5c189f5a03',
     'Clean Code Session',
     'Principes SOLID, clean architecture, et code review.',
     '2025-02-18 14:00:00+03', '2025-02-18 18:00:00+03', 'Antananarivo'),

    -- === 2026 — à venir ===
    ('48c93d68-b51a-429f-afa5-9000b15cfbaa',
     'Meetup Spring Boot',
     'Spring Security, JWT, et tests d''intégration.',
     '2026-06-20 14:00:00+03', '2026-06-20 18:00:00+03', 'Antananarivo'),

    ('1bbda7b9-84fc-40a7-8f07-637d66e2cd7a',
     'Tech Days 2026',
     'Conférence tech annuelle — IA, cloud, cybersécurité, et DevOps.',
     '2026-12-01 09:00:00+03', '2026-12-03 18:00:00+03', 'Antananarivo'),

    ('d78f3851-0cd7-4fa1-8f22-dcf6b2b491c7',
     'Rust Programming Bootcamp',
     'Programmation système avec Rust — ownership, borrowing, et concurrence.',
     '2026-07-15 09:00:00+03', '2026-07-19 17:00:00+03', 'Fianarantsoa'),

    ('03b2a144-f379-4e54-b314-098e58707924',
     'Data Engineering Summit',
     'Spark, Kafka, Airflow, et data pipelines en production.',
     '2026-09-10 09:00:00+02', '2026-09-12 18:00:00+02', 'Antananarivo'),

    ('29cf7ccf-fe72-4c6a-9706-81d417fd6b82',
     'Go Language Meetup',
     'Go pour les microservices, la concurrence, et les outils CLI.',
     '2026-08-05 14:00:00+03', '2026-08-05 18:00:00+03', 'Toamasina'),

    ('7f6a82cf-f59c-4264-874e-1f7bda548104',
     'Machine Learning Ops',
     'MLflow, Kubeflow, et déploiement de modèles en production.',
     '2026-10-20 09:00:00+03', '2026-10-22 17:00:00+03', 'Antananarivo'),

    ('f4dde4b1-9d66-4227-8f66-1b3a4a2d26fd',
     'Security CTF Challenge',
     'Compétition CTF — reverse, pwn, web, et crypto.',
     '2026-11-10 08:00:00+03', '2026-11-11 20:00:00+03', 'Fianarantsoa'),

    ('a3d21b5d-cd56-4fd5-833d-2c92ce6c7340',
     'Frontend Architecture',
     'React, Next.js, state management, et performance web.',
     '2026-05-10 09:00:00+03', '2026-05-10 17:00:00+03', 'Mahajanga'),

    ('2b738f31-7d18-4f97-aa54-9bd0416bc56c',
     'Testing & QA Conference',
     'TDD, tests d''intégration, E2E, et stratégies de QA.',
     '2026-04-15 09:00:00+03', '2026-04-17 18:00:00+03', 'Antananarivo'),

    ('11a1075e-992a-44df-8089-de7ce1f5e79f',
     'Blockchain & Web3',
     'Smart contracts, Solidity, Ethereum, et applications décentralisées.',
     '2026-03-01 09:00:00+03', '2026-03-03 17:00:00+03', 'Toamasina'),

    -- === Même lieu, noms proches (pour tester ILIKE) ===
    ('c0c59fa0-be24-4994-bc6c-884dbb1953c8',
     'Spring Boot Advanced',
     'AOP, caching, sécurité avancée, et tests avec Spring Boot.',
     '2026-02-10 10:00:00+03', '2026-02-10 17:00:00+03', 'Antananarivo'),

    ('4ec5228a-44ee-45d3-a099-8b9e67045db5',
     'Spring Cloud Workshop',
     'Microservices avec Spring Cloud, discovery, et circuit breaker.',
     '2026-01-25 09:00:00+03', '2026-01-25 16:00:00+03', 'Antananarivo')
ON CONFLICT (title) DO NOTHING;
