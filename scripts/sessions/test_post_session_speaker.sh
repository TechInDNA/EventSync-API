#!/bin/bash
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

SESSION_ID="4795f35f-e4d9-4ee3-a7f2-c420ae445bbb"
SPEAKER_ID="c9d22036-5e44-4f70-9199-13a73cc1119f"

echo "=== SUCCESS (201) ==="
echo ""

echo "=== Test #1: [201] link speaker to session with valid timestamps ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo ""

echo "=== VALIDATION ERRORS (422) — startTime / endTime ==="
echo ""

echo "=== Test #2: [422] startTime null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo "=== Test #3: [422] startTime null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":null,"endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo "=== Test #4: [422] endTime null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00"}'

echo ""
echo "=== Test #5: [422] endTime null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":null}'

echo ""
echo "=== Test #6: [422] endTime before startTime ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T14:00:00+03:00","endTime":"2026-08-02T10:00:00+03:00"}'

echo ""
echo "=== Test #7: [422] endTime equal to startTime ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T10:00:00+03:00"}'

echo ""
echo "=== Test #8: [422] invalid time format (not ISO offset) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"invalid","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo "=== Test #9: [422] time without timezone (no offset) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo ""

echo "=== NOT FOUND (404) ==="
echo ""

echo "=== Test #10: [404] non-existent session ID ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/00000000-0000-0000-0000-000000000001/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo "=== Test #11: [404] non-existent speaker ID ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/00000000-0000-0000-0000-000000000002" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo ""

echo "=== MALFORMED REQUEST (400) ==="
echo ""

echo "=== Test #12: [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=== UNAUTHORIZED (401) ==="
echo ""

echo "=== Test #13: [401] no JWT cookie ==="
curlie -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo "=== Test #14: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo ""

echo "=== FORBIDDEN (403) ==="
echo ""

echo "=== Test #15: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T10:00:00+03:00","endTime":"2026-08-02T11:30:00+03:00"}'

echo ""
echo ""

echo "=== CONFLICT (409) ==="
echo ""

echo "=== Test #16: [409] overlapping time slot in same room (pre-seeded 14:00-15:30) ==="
curlie -b /tmp/eventsync_admin.txt -X POST ":8080/sessions/$SESSION_ID/speaker/$SPEAKER_ID" -H 'Content-Type: application/json' -d '{"startTime":"2026-08-02T15:00:00+03:00","endTime":"2026-08-02T16:00:00+03:00"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
