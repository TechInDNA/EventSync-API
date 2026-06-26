-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing DELETE /sessions/{sessionId}/speaker/{speakerId}
-- ═══════════════════════════════════════════════════════════════════════

-- Room needed as FK for sessions
INSERT INTO eventsync_app.room (id, name)
VALUES ('d4e5f6a7-b8c9-0123-def4-567890abcdef', 'Delete Speaker Link Room')
ON CONFLICT (name) DO NOTHING;

-- Event needed as FK for sessions
INSERT INTO eventsync_app.event (id, title, description, start_date, end_date, location)
VALUES (
    'e5f6a7b8-c901-234d-ef56-7890abcdef01',
    'Delete Speaker Link Event',
    'Événement pour tester la suppression du lien intervenant-session.',
    '2077-06-01 09:00:00+03',
    '2077-06-03 18:00:00+03',
    'Antananarivo'
)
ON CONFLICT (title) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Speaker for linked session-speaker test
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, bio, profile_picture, role)
VALUES (
    'f6a7b8c9-0123-4def-5678-90abcdef0123',
    'Link',
    'SpeakerDelete',
    'link.speakerdelete@example.com',
    'Speaker to be unlinked from session.',
    'https://example.com/avatars/link-speaker-delete.jpg',
    'SPEAKER'
)
ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
--  Session — linked to speaker
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO eventsync_app.session (id, title, description, start_date, end_date, room_id, capacity, event_id)
VALUES (
    'a7b8c9d0-1234-4ef5-6789-0abcdef01234',
    'Delete Speaker Link Session',
    'Session avec un intervenant à désassocier.',
    '2077-06-01 10:00:00+03',
    '2077-06-01 12:00:00+03',
    'd4e5f6a7-b8c9-0123-def4-567890abcdef',
    30,
    'e5f6a7b8-c901-234d-ef56-7890abcdef01'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO eventsync_app.session_speaker (session_id, speaker_id, start_time, end_time)
VALUES (
    'a7b8c9d0-1234-4ef5-6789-0abcdef01234',
    'f6a7b8c9-0123-4def-5678-90abcdef0123',
    '2077-06-01T10:00:00+03:00',
    '2077-06-01T12:00:00+03:00'
)
ON CONFLICT DO NOTHING;
