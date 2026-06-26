#!/bin/bash

# ──────────────────────────────────────────────
#  DELETE /sessions/{sessionId}/speaker/{speakerId} — Test script
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Login admin ==="
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
curlie -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo "=== Test #2: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #3: [400] invalid session UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/not-a-uuid/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo "=== Test #4: [400] invalid speaker UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/not-a-uuid

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #5: [404] non-existent session UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/00000000-0000-0000-0000-000000000000/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo "=== Test #6: [404] non-existent speaker UUID ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/00000000-0000-0000-0000-000000000000

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #7: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (204)"
echo "=========================================="
echo ""

echo "=== Test #8: [204] delete existing speaker link ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

echo ""
echo "=== Test #9: [404] delete same link again (already removed) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE :8080/sessions/a7b8c9d0-1234-4ef5-6789-0abcdef01234/speaker/f6a7b8c9-0123-4def-5678-90abcdef0123

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
