#!/bin/bash

# ──────────────────────────────────────────────
#  POST /sessions/{id}/questions — Test script
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Participant token (Alice Dupont) ==="
curlie -s -c /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"firstName":"Alice","lastName":"Dupont","email":"alice@example.com"}' :8080/auth/participant > /dev/null

echo ""
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/questions_admin.txt -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' :8080/auth/login > /dev/null

echo ""
echo ""

SESSION_ID="4089df22-c66d-4c9c-bfa9-8bcec8d2e943"
BAD_SESSION_ID="00000000-0000-0000-0000-000000000099"

echo "=========================================="
echo "  SUCCESS (201)"
echo "=========================================="
echo ""

echo "=== Test #1: [201] POST /sessions/{id}/questions with valid all fields — participant JWT ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Hot Reload Support","content":"Explaining how Spring Boot DevTools hot reload works with AOP aspects"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #2: [201] POST /sessions/{id}/questions with isAnonymous: true ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Caching Best Practices","content":"Using Cacheable annotation versus manual caching strategies","isAnonymous":true}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #3: [201] POST /sessions/{id}/questions with admin JWT ==="
curlie -b /tmp/questions_admin.txt -H 'Content-Type: application/json' -d '{"title":"Spring Profiles","content":"Managing multiple environment configurations in Spring Boot applications"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #4: [404] non-existent session UUID ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Missing Session","content":"This session does not exist"}' :8080/sessions/${BAD_SESSION_ID}/questions

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — title"
echo "=========================================="
echo ""

echo "=== Test #5: [422] title empty ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"","content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #6: [422] title blank (spaces only) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"   ","content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #7: [422] title null (missing field) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #8: [422] title null (explicit null) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":null,"content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #9: [422] title invalid characters (XSS) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"<script>alert(1)</script>","content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #10: [422] title too long (>50 chars) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"ThisQuestionTitleIsWayTooLongAndShouldBeRejectedByValidator","content":"Some content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — content"
echo "=========================================="
echo ""

echo "=== Test #11: [422] content empty ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Valid Title","content":""}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #12: [422] content blank (spaces only) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Valid Title","content":"   "}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #13: [422] content null (missing field) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Valid Title"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #14: [422] content null (explicit null) ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Valid Title","content":null}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #15: [422] content too long (>1000 chars) ==="
LONG_CONTENT=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d "{\"title\":\"Valid Title\",\"content\":\"$LONG_CONTENT\"}" :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #16: [422] content invalid characters <tag> ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{"title":"Valid Title","content":"Using <b>bold</b> tags in content"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #17: [400] invalid JSON body ==="
curlie -b /tmp/questions_participant.txt -H 'Content-Type: application/json' -d '{broken}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #18: [401] no JWT cookie ==="
curlie -H 'Content-Type: application/json' -d '{"title":"No Auth","content":"No token provided"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""
echo "=== Test #19: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -H 'Content-Type: application/json' -d '{"title":"Bad Token","content":"Invalid JWT"}' :8080/sessions/${SESSION_ID}/questions

echo ""
echo ""

rm -f /tmp/questions_participant.txt /tmp/questions_admin.txt
