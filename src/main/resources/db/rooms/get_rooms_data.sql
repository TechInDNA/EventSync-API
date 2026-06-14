INSERT INTO eventsync_app."room" (id, name)
VALUES
    ('cfca8633-9500-4d3d-822d-ef59a4d7b03e', 'Main Hall'),
    ('e36ec732-d4f9-470b-bc66-31323afe15bf', 'Meeting Room A'),
    ('149745a5-8045-4aaa-9a46-c77fe3941295', 'Meeting Room B'),
    ('043714db-17ac-451a-a1fa-e37820cb8e68', 'Salle de Conférence'),
    ('f35d67c7-9c67-42e2-9467-dde95bfcb572', 'Salle de Formation'),
    ('80e587de-7090-4c76-9cfa-5ff0a0e23cd6', 'Salle Réunion'),
    ('0d023e66-4384-4416-a92d-27a4af300ee6', 'Auditorium'),
    ('8507bfbc-f7f2-4f7b-b755-ed03fa604cbd', 'Atelier Python'),
    ('2af27a0c-9513-4ec3-9cf3-e15bb06bb03e', 'Atelier DevOps'),
    ('a8b30763-8f9f-438b-8f63-b487dd8d7a96', 'Workshop Area'),
    ('f6e3f343-2a3f-44df-8af6-057386a9e219', 'Espace Détente'),
    ('8a8081e0-c06c-4624-a5d4-d6e72d0b0ce2', 'Salle de Presse'),
    ('a12e1c3e-56fc-4b07-b3bf-e003f1171086', 'Salon VIP'),
    ('82f370be-6a8c-4931-9749-f0bfe536e4b5', 'Backstage Room'),
    ('2e7c7e28-c026-4f34-9e27-3a7c7224abc8', 'Rooftop Terrace')
ON CONFLICT (name) DO NOTHING;
