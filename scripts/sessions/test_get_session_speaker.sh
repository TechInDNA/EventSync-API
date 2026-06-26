#!/bin/bash

# ──────────────────────────────────────────────
#  GET /sessions/{sessionId}/speaker/{speakerId} — Test script
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #1: [400] invalid session UUID ==="
curlie -X GET :8080/sessions/not-a-uuid/speaker/c9d22036-5e44-4f70-9199-13a73cc1119f

echo ""
echo "=== Test #2: [400] invalid speaker UUID ==="
curlie -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/not-a-uuid

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #3: [404] non-existent session UUID ==="
curlie -X GET :8080/sessions/00000000-0000-0000-0000-000000000000/speaker/c9d22036-5e44-4f70-9199-13a73cc1119f

echo ""
echo "=== Test #4: [404] non-existent speaker UUID ==="
curlie -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/00000000-0000-0000-0000-000000000000

echo ""
echo "=== Test #5: [404] valid session+speaker but no link ==="
curlie -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/11111111-1111-1111-1111-111111111111

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #6: [200] get existing speaker link ==="
curlie -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/c9d22036-5e44-4f70-9199-13a73cc1119f

echo ""
echo "=== Test #7: [200] participant JWT can also GET ==="
curlie -b /tmp/eventsync_participant.txt -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/c9d22036-5e44-4f70-9199-13a73cc1119f

echo ""
echo "=== Test #8: [200] no JWT at all can also GET ==="
curlie -X GET :8080/sessions/4795f35f-e4d9-4ee3-a7f2-c420ae445bbb/speaker/c9d22036-5e44-4f70-9199-13a73cc1119f

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
