-- Seed: sample events for testing filters
-- UUIDs are auto-generated via gen_random_uuid()
INSERT INTO eventsync_app.event (title, description, start_date, end_date, location)
VALUES
    -- === 2025 — passés ===
    ('Conférence Java 2025',
     'Java et Spring Boot — virtual threads, performance, et architecture moderne.',
     '2025-08-15 09:00:00+02', '2025-08-17 18:00:00+02', 'Antananarivo'),

    ('Hackathon Cybersec',
     'CTF, ateliers sécurité offensive et défensive, et conférences.',
     '2025-09-01 08:00:00+03', '2025-09-02 20:00:00+03', 'Fianarantsoa'),

    ('Workshop PostgreSQL',
     'Performance, tuning, indexation, et requêtes avancées.',
     '2025-07-10 10:00:00+03', '2025-07-10 16:00:00+03', 'Toamasina'),

    ('DevOps Meetup',
     'CI/CD, Docker, Kubernetes, et Infrastructure as Code.',
     '2025-11-05 14:00:00+03', '2025-11-05 17:00:00+03', 'Antananarivo'),

    ('AI Summer School',
     'Deep learning, NLP, et vision par ordinateur — 5 jours intensifs.',
     '2025-10-01 09:00:00+03', '2025-10-05 17:00:00+03', 'Mahajanga'),

    ('React Native Workshop',
     'Développement mobile cross-platform avec React Native et Expo.',
     '2025-06-15 09:00:00+03', '2025-06-15 17:00:00+03', 'Antananarivo'),

    ('Kubernetes Hands-on',
     'Déploiement, scaling, et monitoring de clusters K8s.',
     '2025-04-20 10:00:00+03', '2025-04-20 18:00:00+03', 'Fianarantsoa'),

    ('API Design & GraphQL',
     'Bonnes pratiques REST, GraphQL, et documentation avec OpenAPI.',
     '2025-05-10 08:00:00+03', '2025-05-10 16:00:00+03', 'Antananarivo'),

    ('Cloud Native Day',
     'AWS, Azure, GCP — comparaison et migration cloud.',
     '2025-03-22 09:00:00+03', '2025-03-22 17:00:00+03', 'Toamasina'),

    ('Clean Code Session',
     'Principes SOLID, clean architecture, et code review.',
     '2025-02-18 14:00:00+03', '2025-02-18 18:00:00+03', 'Antananarivo'),

    -- === 2026 — à venir ===
    ('Meetup Spring Boot',
     'Spring Security, JWT, et tests d''intégration.',
     '2026-06-20 14:00:00+03', '2026-06-20 18:00:00+03', 'Antananarivo'),

    ('Tech Days 2026',
     'Conférence tech annuelle — IA, cloud, cybersécurité, et DevOps.',
     '2026-12-01 09:00:00+03', '2026-12-03 18:00:00+03', 'Antananarivo'),

    ('Rust Programming Bootcamp',
     'Programmation système avec Rust — ownership, borrowing, et concurrence.',
     '2026-07-15 09:00:00+03', '2026-07-19 17:00:00+03', 'Fianarantsoa'),

    ('Data Engineering Summit',
     'Spark, Kafka, Airflow, et data pipelines en production.',
     '2026-09-10 09:00:00+02', '2026-09-12 18:00:00+02', 'Antananarivo'),

    ('Go Language Meetup',
     'Go pour les microservices, la concurrence, et les outils CLI.',
     '2026-08-05 14:00:00+03', '2026-08-05 18:00:00+03', 'Toamasina'),

    ('Machine Learning Ops',
     'MLflow, Kubeflow, et déploiement de modèles en production.',
     '2026-10-20 09:00:00+03', '2026-10-22 17:00:00+03', 'Antananarivo'),

    ('Security CTF Challenge',
     'Compétition CTF — reverse, pwn, web, et crypto.',
     '2026-11-10 08:00:00+03', '2026-11-11 20:00:00+03', 'Fianarantsoa'),

    ('Frontend Architecture',
     'React, Next.js, state management, et performance web.',
     '2026-05-10 09:00:00+03', '2026-05-10 17:00:00+03', 'Mahajanga'),

    ('Testing & QA Conference',
     'TDD, tests d''intégration, E2E, et stratégies de QA.',
     '2026-04-15 09:00:00+03', '2026-04-17 18:00:00+03', 'Antananarivo'),

    ('Blockchain & Web3',
     'Smart contracts, Solidity, Ethereum, et applications décentralisées.',
     '2026-03-01 09:00:00+03', '2026-03-03 17:00:00+03', 'Toamasina'),

    -- === Même lieu, noms proches (pour tester ILIKE) ===
    ('Spring Boot Advanced',
     'AOP, caching, sécurité avancée, et tests avec Spring Boot.',
     '2026-02-10 10:00:00+03', '2026-02-10 17:00:00+03', 'Antananarivo'),

    ('Spring Cloud Workshop',
     'Microservices avec Spring Cloud, discovery, et circuit breaker.',
     '2026-01-25 09:00:00+03', '2026-01-25 16:00:00+03', 'Antananarivo')
ON CONFLICT (title) DO NOTHING;
