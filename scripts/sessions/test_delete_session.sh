#!/bin/bash

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  AUTH ERRORS (401)"
echo "=========================================="
echo ""

echo "=== Test #1: [401] no JWT cookie ==="
curlie -X DELETE :8080/sessions/238be27f-0f72-47ba-9345-ea2f30f1ce19

echo ""
echo "=== Test #2: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X DELETE :8080/sessions/238be27f-0f72-47ba-9345-ea2f30f1ce19

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #3: [400] invalid UUID (not-a-uuid) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/not-a-uuid

echo ""
echo "=== Test #4: [400] invalid UUID (12345) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/12345

echo ""
echo "=== Test #5: [400] invalid UUID (too-long-with-hyphens) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/too-long-with-hyphens

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #6: [404] non-existent UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/00000000-0000-0000-0000-000000000000

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #7: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X DELETE :8080/sessions/238be27f-0f72-47ba-9345-ea2f30f1ce19

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (204)"
echo "=========================================="
echo ""

echo "=== Test #8: [204] delete session with speaker (cascade to session_speaker) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/238be27f-0f72-47ba-9345-ea2f30f1ce19

echo ""
echo "=== Test #9: [404] delete same session again (now gone) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/238be27f-0f72-47ba-9345-ea2f30f1ce19

echo ""
echo "=== Test #10: [204] delete session without speaker ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/18aee44e-ab74-4316-b65e-b03838d73a5b

echo ""
echo "=== Test #11: [404] delete standalone session again (now gone) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/18aee44e-ab74-4316-b65e-b03838d73a5b

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
