-- Seed: single event with fixed UUID for testing DELETE /events/{id}
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'e1e2e3e4-e5e6-7890-abcd-ef1234567890',
    'Delete Test Event',
    'Événement créé pour tester la suppression. À supprimer après validation.',
    '2077-01-01 09:00:00+03',
    '2077-01-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;
