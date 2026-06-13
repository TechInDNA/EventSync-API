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
curlie -X DELETE :8080/events/4ec5228a-44ee-45d3-a099-8b9e67045db5

echo ""
echo "=== Test #2: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X DELETE :8080/events/4ec5228a-44ee-45d3-a099-8b9e67045db5

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #3: [400] invalid UUID (not-a-uuid) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/not-a-uuid

echo ""
echo "=== Test #4: [400] invalid UUID (12345) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/12345

echo ""
echo "=== Test #5: [400] invalid UUID (too-long-with-hyphens) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/too-long-with-hyphens

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #6: [404] non-existent UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/00000000-0000-0000-0000-000000000000

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #7: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X DELETE :8080/events/4ec5228a-44ee-45d3-a099-8b9e67045db5

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (204)"
echo "=========================================="
echo ""

echo "=== Test #8: [204] delete existing event (seed: Spring Cloud Workshop) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/4ec5228a-44ee-45d3-a099-8b9e67045db5

echo ""
echo "=== Test #9: [404] delete same event again (now gone) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/events/4ec5228a-44ee-45d3-a099-8b9e67045db5

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
