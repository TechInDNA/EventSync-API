#!/bin/bash

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  PREREQUISITES — Seed events for update"
echo "=========================================="
echo ""

# Create events that will be updated later; capture their IDs.
EVENT_1_ID=$(curlie -s -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Alpha","description":"Event to be updated to a new title","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
echo "  → Created event Alpha  id=$EVENT_1_ID"

EVENT_2_ID=$(curlie -s -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Beta","description":"Event used for duplicate-title conflict","startDate":"2026-11-01T09:00:00Z","endDate":"2026-11-03T18:00:00Z","location":"Paris"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
echo "  → Created event Beta   id=$EVENT_2_ID"

EVENT_3_ID=$(curlie -s -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Gamma","description":"Event with past dates for isLive checks","startDate":"2025-01-01T09:00:00Z","endDate":"2025-01-03T18:00:00Z","location":"London"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
echo "  → Created event Gamma  id=$EVENT_3_ID"

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  SUCCESS (200)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #1: [200] PUT /events/{id} update all fields ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Alpha Updated","description":"This event has been updated successfully","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-04T18:00:00Z","location":"Tana"}'

echo ""
echo ""
echo "=== Test #2: [200] PUT /events/{id} update only title (keep others) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_2_ID" -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Beta Renamed","description":"Event used for duplicate-title conflict","startDate":"2026-11-01T09:00:00Z","endDate":"2026-11-03T18:00:00Z","location":"Paris"}'

echo ""
echo ""
echo "=== Test #3: [200] PUT /events/{id} with isLive:false (past dates) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_3_ID" -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Gamma","description":"Past event should be not live","startDate":"2025-01-01T09:00:00Z","endDate":"2025-01-03T18:00:00Z","location":"London"}'

echo ""
echo ""
echo "=== Test #4: [200] PUT /events/{id} spanning now — expect isLive:true in response ==="
NOW_MINUS_2=$(date -u -d '-2 days' +%Y-%m-%dT%H:%M:%SZ)
NOW_PLUS_7=$(date -u -d '+7 days' +%Y-%m-%dT%H:%M:%SZ)
# Create then immediately update
LIVE_EVENT_ID=$(curlie -s -b /tmp/eventsync_admin.txt -X POST :8080/events -H 'Content-Type: application/json' \
  -d '{"title":"PUT Live Test Event","description":"Testing isLive after PUT","startDate":"2025-01-01T09:00:00Z","endDate":"2025-01-03T18:00:00Z","location":"Paris"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$LIVE_EVENT_ID" -H 'Content-Type: application/json' \
  -d "$(printf '{"title":"PUT Live Test Event","description":"Testing isLive after PUT","startDate":"%s","endDate":"%s","location":"Paris"}' "$NOW_MINUS_2" "$NOW_PLUS_7")"

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  NOT FOUND (404)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #5: [404] PUT /events/{id} with non-existent UUID ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/00000000-0000-0000-0000-000000000000 -H 'Content-Type: application/json' \
  -d '{"title":"Nowhere Event","description":"Does not exist","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Nowhere"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  CONFLICT (409)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #6: [409] PUT /events/{id} with duplicate title ===="
# Update Alpha to Beta's original title — should conflict
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"PUT Test Event Beta","description":"This title already exists","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  VALIDATION ERRORS (422) — title
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  VALIDATION ERRORS (422) — title"
echo "=========================================="
echo ""

echo "=== Test #7: [422] title empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #8: [422] title blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"   ","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #9: [422] title null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #10: [422] title null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":null,"description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #11: [422] title invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"<script>alert(1)</script>","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #12: [422] title too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  VALIDATION ERRORS (422) — description
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  VALIDATION ERRORS (422) — description"
echo "=========================================="
echo ""

echo "=== Test #13: [422] description empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #14: [422] description null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #15: [422] description null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":null,"startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #16: [422] description too long (>1000 chars) ==="
LONG_DESC=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d "{\"title\":\"Valid Title\",\"description\":\"$LONG_DESC\",\"startDate\":\"2026-10-01T09:00:00Z\",\"endDate\":\"2026-10-03T18:00:00Z\",\"location\":\"Antananarivo\"}"

echo ""
echo "=== Test #17: [422] description with invalid characters ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Description with <html> tags","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  VALIDATION ERRORS (422) — location
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  VALIDATION ERRORS (422) — location"
echo "=========================================="
echo ""

echo "=== Test #18: [422] location empty ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":""}'

echo ""
echo "=== Test #19: [422] location blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"   "}'

echo ""
echo "=== Test #20: [422] location null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z"}'

echo ""
echo "=== Test #21: [422] location null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":null}'

echo ""
echo "=== Test #22: [422] location too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"ThisLocationNameIsWayTooLongAndShouldBeRejectedByValidator"}'

echo ""
echo "=== Test #23: [422] location invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"<script>alert(1)</script>"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  VALIDATION ERRORS (422) — dates
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  VALIDATION ERRORS (422) — dates"
echo "=========================================="
echo ""

echo "=== Test #24: [422] startDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #25: [422] startDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":null,"endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #26: [422] endDate null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #27: [422] endDate null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":null,"location":"Antananarivo"}'

echo ""
echo "=== Test #28: [422] endDate before startDate ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-03T09:00:00Z","endDate":"2026-10-01T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #29: [422] endDate equal to startDate ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-01T09:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  MALFORMED REQUEST (400)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #30: [400] invalid UUID (not-a-uuid) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/not-a-uuid -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #31: [400] invalid UUID (12345) ==="
curlie -b /tmp/eventsync_admin.txt -X PUT :8080/events/12345 -H 'Content-Type: application/json' \
  -d '{"title":"Valid Title","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #32: [400] malformed JSON body ==="
curlie -b /tmp/eventsync_admin.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  UNAUTHORIZED (401)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #33: [401] no JWT cookie ==="
curlie -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"No Auth Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo "=== Test #34: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Bad Token Event","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""
echo ""

# ──────────────────────────────────────────────────────────────────────
#  FORBIDDEN (403)
# ──────────────────────────────────────────────────────────────────────

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #35: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X PUT ":8080/events/$EVENT_1_ID" -H 'Content-Type: application/json' \
  -d '{"title":"Participant Update","description":"Desc","startDate":"2026-10-01T09:00:00Z","endDate":"2026-10-03T18:00:00Z","location":"Antananarivo"}'

echo ""

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
