-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing DELETE /sessions/{id}
-- ═══════════════════════════════════════════════════════════════════════

-- Room needed as FK for sessions
INSERT INTO eventsync_app.room (id, name)
VALUES ('d1111111-1111-1111-1111-111111111111', 'Delete Session Room')
ON CONFLICT (name) DO NOTHING;

-- Event needed as FK for sessions
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'd2222222-2222-2222-2222-222222222222',
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
    'd3333333-3333-3333-3333-333333333333',
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
    'd4444444-4444-4444-4444-444444444444',
    'Delete Test - Session With Speaker',
    'Session liée à un intervenant, à supprimer.',
    '2077-06-01 10:00:00+03',
    '2077-06-01 12:00:00+03',
    'd1111111-1111-1111-1111-111111111111',
    30,
    'd2222222-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id)
VALUES ('d4444444-4444-4444-4444-444444444444', 'd3333333-3333-3333-3333-333333333333')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Session #2 — no speaker attached
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'd5555555-5555-5555-5555-555555555555',
    'Delete Test - Session Without Speaker',
    'Session sans intervenant, à supprimer.',
    '2077-06-02 14:00:00+03',
    '2077-06-02 16:00:00+03',
    'd1111111-1111-1111-1111-111111111111',
    20,
    'd2222222-2222-2222-2222-222222222222'
)
ON CONFLICT (title) DO NOTHING;
