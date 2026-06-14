#!/bin/bash

EVENT_WITH_SESSIONS="7cc3b7d6-917c-4b34-85ce-8be092b0dca3"
EVENT_STANDALONE="1c035819-07c6-4a97-a938-6d5859a7d755"
UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"

echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #1: [200] PUT /events/{id} — update event with sessions (expect sessions array in response) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_WITH_SESSIONS -H 'Content-Type: application/json' -d '{"title":"PUT Test - Event With Sessions","description":"Updated description for the event with sessions","startDate":"2026-12-01T09:00:00Z","endDate":"2026-12-05T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""
echo "=== Test #2: [200] PUT /events/{id} — update standalone event (expect no sessions in response) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"PUT Test - Event Standalone","description":"Updated standalone event","startDate":"2027-01-15T09:00:00Z","endDate":"2027-01-17T18:00:00Z","location":"Lyon"}'

echo ""
echo ""
echo "=== Test #3: [200] PUT /events/{id} — update only title, keep other fields unchanged ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_WITH_SESSIONS -H 'Content-Type: application/json' -d '{"title":"PUT Test - Event With Sessions Renamed","description":"Updated description for the event with sessions","startDate":"2026-12-01T09:00:00Z","endDate":"2026-12-05T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #4: [404] PUT /events/{id} with non-existent UUID ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$UNKNOWN_UUID -H 'Content-Type: application/json' -d '{"title":"Nowhere Event","description":"Does not exist","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Nowhere"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "--- Setup: create a second event for duplicate-title test ---"
CONFLICT_TITLE="PUT Test - Conflict Target"
curlie -s -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' -d "{\"title\":\"$CONFLICT_TITLE\",\"description\":\"Target for conflict test\",\"startDate\":\"2026-12-01T09:00:00Z\",\"endDate\":\"2026-12-05T18:00:00Z\",\"location\":\"Antananarivo\"}" > /dev/null

echo "=== Test #5: [409] PUT /events/{id} with title that already exists (unique constraint) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d "{\"title\":\"$CONFLICT_TITLE\",\"description\":\"This title already exists\",\"startDate\":\"2027-01-15T09:00:00Z\",\"endDate\":\"2027-01-17T18:00:00Z\",\"location\":\"Lyon\"}"

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — title"
echo "=========================================="
echo ""

echo "=== Test #6: [422] title empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #7: [422] title blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"   ","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #8: [422] title null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #9: [422] title null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":null,"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #10: [422] title invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"<script>alert(1)</script>","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #11: [422] title too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — description"
echo "=========================================="
echo ""

echo "=== Test #12: [422] description empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #13: [422] description null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #14: [422] description null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":null,"startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #15: [422] description too long (>1000 chars) ==="
LONG_DESC=$(python3 -c "import sys; sys.stdout.write('x' * 1001)")
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d "{\"title\":\"Valid Title\",\"description\":\"$LONG_DESC\",\"startDate\":\"2026-10-01T09:00:00Z\",\"endDate\":\"2026-10-03T18:00:00Z\",\"location\":\"Antananarivo\"}"

echo ""
echo "=== Test #16: [422] description with invalid characters ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Description with <html> tags","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — location"
echo "=========================================="
echo ""

echo "=== Test #17: [422] location empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":""}'

echo ""
echo "=== Test #18: [422] location blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"   "}'

echo ""
echo "=== Test #19: [422] location null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z"}'

echo ""
echo "=== Test #20: [422] location null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":null}'

echo ""
echo "=== Test #21: [422] location too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"ThisLocationNameIsWayTooLongAndShouldBeRejectedByValidator"}'

echo ""
echo "=== Test #22: [422] location invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"<script>alert(1)</script>"}'

echo ""
echo "=== Test #23: [422] location starting with non-letter (format) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"-invalid-start"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — dates"
echo "=========================================="
echo ""

echo "=== Test #24: [422] startDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #25: [422] startDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":null,"endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #26: [422] endDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #27: [422] endDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":null,"location":"Antananarivo"}'

echo ""
echo "=== Test #28: [422] endDate before startDate ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-03T09:00:00Z","endDate":"2026-10-01T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #29: [422] endDate equal to startDate ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #30: [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""
echo "=== Test #31: [400] invalid UUID (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/not-a-uuid -H 'Content-Type: application/json' -d '{"title":"Bad UUID","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""
echo "=== Test #32: [400] invalid UUID (numeric 12345) — not parseable as UUID ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/12345 -H 'Content-Type: application/json' -d '{"title":"Bad UUID","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #33: [401] no JWT cookie ==="
curlie -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"No Auth Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #34: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Bad Token Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #35: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Participant Update","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
