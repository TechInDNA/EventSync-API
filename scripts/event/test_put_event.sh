#!/bin/bash

SEQUENTIAL_NUMBER=0

EVENT_WITH_SESSIONS="7cc3b7d6-917c-4b34-85ce-8be092b0dca3"
EVENT_STANDALONE="1c035819-07c6-4a97-a938-6d5859a7d755"
UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin_jar.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant_jar.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=== SUCCESS (200) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [200] PUT /events/{id} — update event with sessions (expect sessions array in response) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_WITH_SESSIONS -H 'Content-Type: application/json' -d '{"title":"PUT Test — Event With Sessions","description":"Updated description for the event with sessions","startDate":"2026-12-01T09:00:00Z","endDate":"2026-12-05T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [200] PUT /events/{id} — update standalone event (expect no sessions in response) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"PUT Test — Event Standalone","description":"Updated standalone event","startDate":"2027-01-15T09:00:00Z","endDate":"2027-01-17T18:00:00Z","location":"Lyon"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [200] PUT /events/{id} — update only title, keep other fields unchanged ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_WITH_SESSIONS -H 'Content-Type: application/json' -d '{"title":"PUT Test — Event With Sessions Renamed","description":"Updated description for the event with sessions","startDate":"2026-12-01T09:00:00Z","endDate":"2026-12-05T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=== NOT FOUND (404) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [404] PUT /events/{id} with non-existent UUID ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$UNKNOWN_UUID -H 'Content-Type: application/json' -d '{"title":"Nowhere Event","description":"Does not exist","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Nowhere"}'

echo ""
echo ""

echo "=== CONFLICT (409) ==="
echo ""

echo "--- Setup: create a second event for duplicate-title test ---"
CONFLICT_TITLE="PUT Test — Conflict Target"
curlie -s -b /tmp/eventsync_admin_jar.txt -X POST :8080/events -H 'Content-Type: application/json' -d "{\"title\":\"$CONFLICT_TITLE\",\"description\":\"Target for conflict test\",\"startDate\":\"2026-12-01T09:00:00Z\",\"endDate\":\"2026-12-05T18:00:00Z\",\"location\":\"Antananarivo\"}" > /dev/null

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [409] PUT /events/{id} with title that already exists (unique constraint) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d "{\"title\":\"$CONFLICT_TITLE\",\"description\":\"This title already exists\",\"startDate\":\"2027-01-15T09:00:00Z\",\"endDate\":\"2027-01-17T18:00:00Z\",\"location\":\"Lyon\"}"

echo ""
echo ""

echo "=== VALIDATION ERRORS (422) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"   ","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":null,"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"<script>alert(1)</script>","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] title too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] description empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] description null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] description null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":null,"startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] description too long (>1000 chars) ==="
LONG_DESC=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d "{\"title\":\"Valid Title\",\"description\":\"$LONG_DESC\",\"startDate\":\"2026-10-01T09:00:00Z\",\"endDate\":\"2026-10-03T18:00:00Z\",\"location\":\"Antananarivo\"}"

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] description with invalid characters ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Description with illegal <html> chars","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":""}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"   "}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":null}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"ThisLocationNameIsWayTooLongAndShouldBeRejectedByValidator"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"<script>alert(1)</script>"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] location starting with non-letter (format) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"-invalid-start"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] startDate null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] startDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":null,"endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] endDate null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] endDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":null,"location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] endDate before startDate ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-03T09:00:00Z","endDate":"2026-10-01T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [422] endDate equal to startDate ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=== MALFORMED REQUEST (400) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=== TYPE MISMATCH (400) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [400] invalid UUID (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/not-a-uuid -H 'Content-Type: application/json' -d '{"title":"Bad UUID","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [400] invalid UUID (numeric 12345) — not parseable as UUID ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/events/12345 -H 'Content-Type: application/json' -d '{"title":"Bad UUID","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=== UNAUTHORIZED (401) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [401] no JWT cookie ==="
curlie -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"No Auth Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Bad Token Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

echo "=== FORBIDDEN (403) ==="
echo ""

SEQUENTIAL_NUMBER=$((SEQUENTIAL_NUMBER + 1)); echo "=== [$SEQUENTIAL_NUMBER] [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant_jar.txt -X PUT :8080/events/$EVENT_STANDALONE -H 'Content-Type: application/json' -d '{"title":"Participant Update","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

rm -f /tmp/eventsync_admin_jar.txt /tmp/eventsync_participant_jar.txt
