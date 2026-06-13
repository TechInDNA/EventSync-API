#!/bin/bash

# ──────────────────────────────────────────────
#  POST /sessions — Test script
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Login admin ===" && \
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ===" && \
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (201)"
echo "=========================================="
echo ""

echo "=== Test #1: [201] POST /sessions with valid all fields ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Keynote Spring Boot","description":"Introduction aux nouveautes de Spring Boot 4","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #2: [409] POST /sessions with duplicate title ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Keynote Spring Boot","description":"Duplicate title test","startDate":"2026-07-02T11:00:00Z","endDate":"2026-07-02T12:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":50,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — title"
echo "=========================================="
echo ""

echo "=== Test #3: [422] title empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #4: [422] title blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"   ","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #5: [422] title null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #6: [422] title null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":null,"description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #7: [422] title invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"<script>alert(1)</script>","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #8: [422] title too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"ThisSessionTitleIsWayTooLongAndShouldDefinitelyBeRejectedByValidator","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #9: [422] title starting with non-letter ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"-intro","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #10: [422] title with digits ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Session 123","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — description"
echo "=========================================="
echo ""

echo "=== Test #11: [422] description null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Title","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — dates"
echo "=========================================="
echo ""

echo "=== Test #12: [422] startDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #13: [422] endDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #14: [422] endDate before startDate ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T10:00:00Z","endDate":"2026-07-02T09:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #15: [422] endDate equal to startDate ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T10:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — roomId / eventId"
echo "=========================================="
echo ""

echo "=== Test #16: [422] roomId null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #17: [422] eventId null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — capacity"
echo "=========================================="
echo ""

echo "=== Test #18: [422] capacity null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #19: [422] capacity zero ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":0,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #20: [422] capacity negative ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Valid Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":-5,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #21: [404] non-existent room ID ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Session Bad Room","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"00000000-0000-0000-0000-000000000001","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #22: [404] non-existent event ID ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Session Bad Event","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"00000000-0000-0000-0000-000000000002"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #23: [400] malformed JSON body ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #24: [401] no JWT cookie ==="
curlie -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"No Auth","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo "=== Test #25: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Bad Token","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #26: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X POST :8080/sessions -H 'Content-Type: application/json' -d '{"title":"Participant Session","description":"Desc","startDate":"2026-07-02T09:00:00Z","endDate":"2026-07-02T10:00:00Z","roomId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","capacity":100,"eventId":"b2c3d4e5-f6a7-8901-bcde-f12345678901"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
