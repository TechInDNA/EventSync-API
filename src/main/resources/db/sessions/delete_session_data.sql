-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing DELETE /sessions/{id}
-- ═══════════════════════════════════════════════════════════════════════

-- Room needed as FK for sessions
INSERT INTO eventsync_app.room (id, name)
VALUES ('c4d26451-c924-46dc-ad8b-78e6ba055f05', 'Delete Session Room')
ON CONFLICT (name) DO NOTHING;

-- Event needed as FK for sessions
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    '0d716865-bbc4-42f2-b6c8-ab2c8d912c6b',
    'Delete Session Event',
    'Événement pour tester la suppression de sessions.',
    '2077-06-01 09:00:00+03',
    '2077-06-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- Speaker user needed for session_speaker join
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'c3c6b243-c760-48cf-9ce6-2ebe7de293af',
    'Delete',
    'SessionSpeaker',
    'delete.sessionspeaker@example.com',
    'Speaker linked to a deletable session.',
    'https://example.com/avatars/delete-session-speaker.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Session #1 — linked to a speaker (via session_speaker)
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '238be27f-0f72-47ba-9345-ea2f30f1ce19',
    'Delete Test - Session With Speaker',
    'Session liée à un intervenant, à supprimer.',
    '2077-06-01 10:00:00+03',
    '2077-06-01 12:00:00+03',
    'c4d26451-c924-46dc-ad8b-78e6ba055f05',
    30,
    '0d716865-bbc4-42f2-b6c8-ab2c8d912c6b'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('238be27f-0f72-47ba-9345-ea2f30f1ce19', 'c3c6b243-c760-48cf-9ce6-2ebe7de293af')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Session #2 — no speaker attached
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    '18aee44e-ab74-4316-b65e-b03838d73a5b',
    'Delete Test - Session Without Speaker',
    'Session sans intervenant, à supprimer.',
    '2077-06-02 14:00:00+03',
    '2077-06-02 16:00:00+03',
    'c4d26451-c924-46dc-ad8b-78e6ba055f05',
    20,
    '0d716865-bbc4-42f2-b6c8-ab2c8d912c6b'
)
ON CONFLICT (title) DO NOTHING;
