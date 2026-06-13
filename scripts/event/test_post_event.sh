#!/bin/bash

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (201)"
echo "=========================================="
echo ""

echo "=== Test #1: [201] POST /events with valid minimal fields ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Tech Summit 2026","description":"A great tech summit","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""
echo "=== Test #2: [201] POST /events with future dates ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"AI Conference 2027","description":"Artificial intelligence conference","startDate":"2027-03-01T09:00:00Z","endDate":"2027-03-03T18:00:00Z","location":"Paris"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #3: [409] POST /events with duplicate title + location + overlapping dates ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Tech Summit 2026","description":"Duplicate event","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — title"
echo "=========================================="
echo ""

echo "=== Test #4: [422] title empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #5: [422] title blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"   ","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #6: [422] title null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #7: [422] title null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":null,"description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #8: [422] title invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"<script>alert(1)</script>","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #9: [422] title too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #10: [201] POST /events spanning now — expect isLive:true in response ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d "$(printf '{"title":"Live Event Test","description":"Testing isLive flag","startDate":"%s","endDate":"%s","location":"Antananarivo"}' "$(date -u -d '-1 day' +%Y-%m-%dT%H:%M:%SZ)" "$(date -u -d '+7 days' +%Y-%m-%dT%H:%M:%SZ)")"

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — description"
echo "=========================================="
echo ""

echo "=== Test #11: [422] description empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #12: [422] description null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #13: [422] description null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":null,"startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #14: [422] description too long (>1000 chars) ==="
LONG_DESC=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d "{\"title\":\"Valid Title\",\"description\":\"$LONG_DESC\",\"startDate\":\"2026-09-15T09:00:00Z\",\"endDate\":\"2026-09-17T18:00:00Z\",\"location\":\"Antananarivo\"}"

echo ""
echo "=== Test #15: [422] description with invalid characters ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Description with <html> tags","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — location"
echo "=========================================="
echo ""

echo "=== Test #16: [422] location empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":""}'

echo ""
echo "=== Test #17: [422] location blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"   "}'

echo ""
echo "=== Test #18: [422] location null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z"}'

echo ""
echo "=== Test #19: [422] location null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":null}'

echo ""
echo "=== Test #20: [422] location too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"ThisLocationNameIsWayTooLongAndShouldBeRejectedByValidator"}'

echo ""
echo "=== Test #21: [422] location invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"<script>alert(1)</script>"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — dates"
echo "=========================================="
echo ""

echo "=== Test #22: [422] startDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #23: [422] startDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":null,"endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #24: [422] endDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #25: [422] endDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":null,"location":"Antananarivo"}'

echo ""
echo "=== Test #26: [422] endDate before startDate ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-17T09:00:00Z","endDate":"2026-09-15T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #27: [422] endDate equal to startDate ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-15T09:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #28: [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #29: [401] no JWT cookie ==="
curlie -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"No Auth Event","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #30: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Bad Token Event","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #31: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X POST :8080/events -H 'Content-Type: application/json' -d '{"title":"Participant Event","description":"Desc","startDate":"2026-09-15T09:00:00Z","endDate":"2026-09-17T18:00:00Z","location":"Antananarivo"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
