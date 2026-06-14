
-- ═══════════════════════════════════════════════════════════════════════
--  Event #1 — has 2 sessions attached
-- ═══════════════════════════════════════════════════════════════════════

INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '7cc3b7d6-917c-4b34-85ce-8be092b0dca3',
    'PUT Test — Event With Sessions',
    'Événement avec sessions associées pour tester PUT /events/{id}.',
    '2026-12-01 09:00:00+03',
    '2026-12-05 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '2b4662ad-a180-4967-978b-118485223838',
    'PUT Test — Session Alpha',
    'Première session de l''événement PUT.',
    '2026-12-01 09:00:00+03',
    '2026-12-01 12:00:00+03',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    50,
    '7cc3b7d6-917c-4b34-85ce-8be092b0dca3'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app."session" (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'f4c143db-9849-4294-8c56-c54fde736add',
    'PUT Test — Session Beta',
    'Deuxième session de l''événement PUT.',
    '2026-12-02 09:00:00+03',
    '2026-12-02 17:00:00+03',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    30,
    '7cc3b7d6-917c-4b34-85ce-8be092b0dca3'
)
ON CONFLICT (title) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Event #2 — standalone, no sessions
-- ═══════════════════════════════════════════════════════════════════════

INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '1c035819-07c6-4a97-a938-6d5859a7d755',
    'PUT Test — Event Standalone',
    'Événement sans session pour tester PUT /events/{id}.',
    '2027-01-15 09:00:00+03',
    '2027-01-17 18:00:00+03',
    'Paris'
)
ON CONFLICT (title) DO NOTHING;
