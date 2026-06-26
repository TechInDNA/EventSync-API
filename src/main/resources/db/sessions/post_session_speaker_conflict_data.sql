-- ═══════════════════════════════════════════════════════════════════════
--  Seed data for testing CONFLICT on POST /sessions/{id}/speaker/{id}
--  Pre-seeds a session_speaker row so overlapping time slot triggers 409.
--  Depends on room, event, session, user from post_session_speaker_data.sql.
--  UUID generated via uuidgen.
-- ═══════════════════════════════════════════════════════════════════════

INSERT INTO eventsync_app.session_speaker (id, session_id, speaker_id, start_time, end_time)
VALUES (
    'b82b907a-2074-4b49-9d9e-79ecd3abfdeb',
    '4795f35f-e4d9-4ee3-a7f2-c420ae445bbb',
    'c9d22036-5e44-4f70-9199-13a73cc1119f',
    '2026-08-02 14:00:00+03',
    '2026-08-02 15:30:00+03'
)
ON CONFLICT (id) DO NOTHING;
