#!/bin/bash

SEED_UUID="a1b2c3d4-e5f6-7890-abcd-ef1234567890"
UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin_jar.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant_jar.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=== SUCCESS (200) ==="
echo ""

echo "=== [200] PUT /rooms/{id} with valid new name ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"Renamed Hall"}'

echo ""
echo ""

echo "=== NOT FOUND (404) ==="
echo ""

echo "=== [404] PUT /rooms/{id} with unknown UUID ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$UNKNOWN_UUID -H 'Content-Type: application/json' -d '{"name":"Non Existent Room"}'

echo ""
echo ""

echo "=== CONFLICT (409) ==="
echo ""

echo "--- Setup: create second room for duplicate-name test ---"
SECOND_NAME="Salle Temporaire"
curlie -s -b /tmp/eventsync_admin_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d "{\"name\":\"$SECOND_NAME\"}" > /dev/null

echo "=== [409] PUT /rooms/{id} with name that already exists (unique constraint) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"Salle Temporaire"}'

echo ""
echo ""

echo "=== VALIDATION ERRORS (422) ==="
echo ""

echo "=== [422] empty name ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":""}'

echo ""
echo "=== [422] blank name (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"   "}'

echo ""
echo "=== [422] null name (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{}'

echo ""
echo "=== [422] null name (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":null}'

echo ""
echo "=== [422] invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"<script>alert(1)</script>"}'

echo ""
echo "=== [422] name too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"ThisRoomNameIsWayTooLongAndShouldBeRejectedByTheValidator"}'

echo ""
echo "=== [422] name starting with non-letter (format) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"-invalid-start"}'

echo ""
echo ""

echo "=== MALFORMED REQUEST (400) ==="
echo ""

echo "=== [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=== TYPE MISMATCH (400) ==="
echo ""

echo "=== [400] invalid UUID (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/not-a-uuid -H 'Content-Type: application/json' -d '{"name":"Bad UUID"}'

echo ""
echo "=== [400] invalid UUID (numeric 12345) — not parseable as UUID ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/rooms/12345 -H 'Content-Type: application/json' -d '{"name":"Bad UUID"}'

echo ""
echo ""

echo "=== UNAUTHORIZED (401) ==="
echo ""

echo "=== [401] no JWT cookie ==="
curlie -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"No Auth Room"}'

echo ""
echo "=== [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"Bad Token Room"}'

echo ""
echo ""

echo "=== FORBIDDEN (403) ==="
echo ""

echo "=== [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant_jar.txt -X PUT :8080/rooms/$SEED_UUID -H 'Content-Type: application/json' -d '{"name":"Participant Room"}'

rm -f /tmp/eventsync_admin_jar.txt /tmp/eventsync_participant_jar.txt
