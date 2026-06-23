#!/bin/bash

# ──────────────────────────────────────────────
#  GET /sessions — Test script
#  Prerequisite: run get_sessions_data.sql first
# ──────────────────────────────────────────────

echo "=== AUTH SETUP — Admin login ====" && \
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo ""
echo "=========================================="
echo "  GET /sessions — SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== Test #1: [200] GET /sessions — default pagination (page 1, size 20) ==="
curlie -X GET -b /tmp/eventsync_admin.txt :8080/sessions

echo ""
echo "=== Test #2: [200] GET /sessions — custom pagination (page 1, size 2) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?page=1&size=2'

echo ""
echo "=== Test #3: [200] GET /sessions — filter by live=true (expect 2) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?live=true'

echo ""
echo "=== Test #4: [200] GET /sessions — filter by live=false (expect 4) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?live=false'

echo ""
echo "=== Test #5: [200] GET /sessions — filter by room name ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?room=Session%20Room%20Alpha'

echo ""
echo "=== Test #6: [200] GET /sessions — filter by speaker first name ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?speaker=Alice'

echo ""
echo "=== Test #7: [200] GET /sessions — filter by speaker last name ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?speaker=Builder'

echo ""
echo "=== Test #8: [200] GET /sessions — filter by event title ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?event=Tech%20Summit%202026'

echo ""
echo "=== Test #9: [200] GET /sessions — combined filters (live + speaker) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?live=true&speaker=Carol'

echo ""
echo "=== Test #10: [200] GET /sessions — combined filters (all params) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?room=Session%20Room%20Alpha&event=Tech%20Summit%202026&live=false&speaker=Alice'

echo ""
echo ""
echo "=========================================="
echo "  GET /sessions — VALIDATION ERRORS (422)"
echo "=========================================="
echo ""

echo "=== Test #11: [422] GET /sessions — invalid room (special chars) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?room=@@invalid@@'

echo ""
echo "=== Test #12: [422] GET /sessions — invalid speaker (special chars) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?speaker=%3Cscript%3E'

echo ""
echo "=== Test #13: [422] GET /sessions — invalid event (special chars) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?event=!!!invalid!!!'

echo ""
echo ""
echo "=========================================="
echo "  GET /sessions — EMPTY RESULTS"
echo "=========================================="
echo ""

echo "=== Test #14: [200] GET /sessions — non-matching room (expect empty data) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?room=UnknownRoomName'

echo ""
echo "=== Test #15: [200] GET /sessions — non-matching event (expect empty data) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?event=NonExistentEvent'

echo ""
echo "=== Test #16: [200] GET /sessions — page beyond results (expect empty data) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?page=999'

echo ""
echo ""
echo "=========================================="
echo "  GAPS ADDED"
echo "=========================================="
echo ""

echo "=== Test #17: [200] GET /sessions — no auth cookie (permitAll) ==="
curlie -X GET :8080/sessions | jq '{meta: .meta}' 2>/dev/null || curlie -X GET :8080/sessions

echo ""
echo "=== Test #18: [200] GET /sessions — blank room filter (returns all) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?room='

echo ""
echo "=== Test #19: [200] GET /sessions — non-matching speaker (expect empty data) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?speaker=ZzzNotFound'

echo ""
echo "=== Test #20: [200] GET /sessions — size=0 (clamped to 20, returns all) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?size=0'

echo ""
echo "=== Test #21: [200] GET /sessions — page=0 (clamped to 1) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?page=0'

echo ""
echo "=== Test #22: [400] GET /sessions — live=invalid (type mismatch) ==="
curlie -X GET -b /tmp/eventsync_admin.txt ':8080/sessions?live=notabool'

rm -f /tmp/eventsync_admin.txt
