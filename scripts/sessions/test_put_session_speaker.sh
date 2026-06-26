#!/bin/bash
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

# ──────────────────────────────────────────────────────
#  Seed UUIDs (from db/sessions/put_session_speaker_data.sql)
# ──────────────────────────────────────────────────────
SOLO_SESSION_ID="a0a3f4a0-a694-42cb-85dd-edc611f5b059"
SOLO_SPEAKER_ID="cfec79b8-b71e-40ec-a340-4ceb26a2acbb"
SOLO_LINK_ID="743f3dbd-3706-45b6-adea-f48936aa815e"

MULTI_SESSION_ID="dd99b878-b2db-4805-b607-4b903426d527"
MULTI_SPEAKER_ID="6b74ea7b-bb2b-4e91-ba70-d2f037772083"
MULTI_LINK_ID="22b6fe56-5224-40d1-91e3-07804c156a51"

UNKNOWN_SESSION="00000000-0000-0000-0000-000000000000"
UNKNOWN_SPEAKER="00000000-0000-0000-0000-000000000001"
UNKNOWN_LINK="00000000-0000-0000-0000-000000000002"

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #1: [200] update single-link speaker (Alice solo slot) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:30:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #2: [200] update multi-link speaker first link (Bob Session 2) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$MULTI_SESSION_ID/speaker/$MULTI_SPEAKER_ID?linkId=$MULTI_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T11:30:00+03:00","endTime":"2026-07-02T13:30:00+03:00"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422)"
echo "=========================================="
echo ""

echo "=== Test #3: [422] startTime null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #4: [422] startTime null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":null,"endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #5: [422] endTime null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00"}'

echo ""
echo "=== Test #6: [422] endTime null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":null}'

echo ""
echo "=== Test #7: [422] endTime before startTime ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T12:00:00+03:00","endTime":"2026-07-02T10:00:00+03:00"}'

echo ""
echo "=== Test #8: [422] endTime equal to startTime ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T10:00:00+03:00","endTime":"2026-07-02T10:00:00+03:00"}'

echo ""
echo "=== Test #9: [422] invalid time format (not ISO offset) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"invalid","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #10: [422] time without timezone (no offset) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #11: [404] non-existent session UUID ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$UNKNOWN_SESSION/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #12: [404] non-existent speaker UUID ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$UNKNOWN_SPEAKER?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #13: [404] non-existent linkId (valid session+speaker, wrong link) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$UNKNOWN_LINK" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #14: [400] invalid session UUID format ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/not-a-uuid/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #15: [400] invalid speaker UUID format ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/not-a-uuid?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #16: [400] invalid linkId UUID format ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=not-a-uuid" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #17: [400] missing linkId query parameter ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #18: [400] malformed JSON body ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #19: [401] no JWT cookie ==="
curlie -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo "=== Test #20: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #21: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X PUT ":8080/sessions/$SOLO_SESSION_ID/speaker/$SOLO_SPEAKER_ID?linkId=$SOLO_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T09:00:00+03:00","endTime":"2026-07-02T11:00:00+03:00"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #22: [409] update time slot overlaps another speaker in same room (Bob Session 3 has 14:00-15:30) ==="
# Bob's other link (Session 3) runs 14:00-15:30 in the same room.
# Updating Bob's Session 2 link to 14:30-15:30 overlaps with Session 3's slot.
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/sessions/$MULTI_SESSION_ID/speaker/$MULTI_SPEAKER_ID?linkId=$MULTI_LINK_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-07-02T14:30:00+03:00","endTime":"2026-07-02T15:30:00+03:00"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
