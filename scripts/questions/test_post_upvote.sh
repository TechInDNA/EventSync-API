#!/bin/bash

# ──────────────────────────────────────────────
#  POST /sessions/{id}/questions/{qid}/upvote — Test script
# ──────────────────────────────────────────────
#  Dependencies: run db/upvote/post_upvote_data.sql first
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Participant token (Alice Dupont) ==="
curlie -s -c /tmp/upvote_participant.txt -H 'Content-Type: application/json' -d '{"firstName":"Alice","lastName":"Dupont","email":"alice@example.com"}' :8080/auth/participant > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token (Bob Martin) ==="
curlie -s -c /tmp/upvote_bob.txt -H 'Content-Type: application/json' -d '{"firstName":"Bob","lastName":"Martin","email":"bob.martin@example.com"}' :8080/auth/participant > /dev/null

echo ""
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/upvote_admin.txt -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' :8080/auth/login > /dev/null

echo ""
echo ""

SESSION_ID="ded92053-a92a-4979-b7be-4330e555861c"
QUESTION_ID="3a81ea5b-2acc-4ff0-a277-e9c5c2d724d3"
BAD_QUESTION_ID="00000000-0000-0000-0000-000000000099"

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #1: [200] POST first upvote on question — participant JWT ==="
curlie -b /tmp/upvote_participant.txt -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""
echo "=== Test #2: [200] POST same upvote again (delete + re-insert, count stable) ==="
curlie -b /tmp/upvote_participant.txt -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""
echo "=== Test #3: [200] POST upvote with admin JWT ==="
curlie -b /tmp/upvote_admin.txt -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""
echo "=== Test #4: [200] POST upvote from different participant (Bob) ==="
curlie -b /tmp/upvote_bob.txt -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #5: [404] non-existent question UUID ==="
curlie -b /tmp/upvote_participant.txt -X POST :8080/sessions/${SESSION_ID}/questions/${BAD_QUESTION_ID}/upvote

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #6: [401] no JWT cookie ==="
curlie -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""
echo "=== Test #7: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/sessions/${SESSION_ID}/questions/${QUESTION_ID}/upvote

echo ""
echo ""

rm -f /tmp/upvote_participant.txt /tmp/upvote_admin.txt /tmp/upvote_bob.txt
