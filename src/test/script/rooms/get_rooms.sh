#!/bin/bash

echo "Testing GET /rooms"
echo ""

# ──────────────────────────────────────────
# Login to populate cookie jars
# ──────────────────────────────────────────
echo "--- Obtaining admin JWT cookie ---"
curlie -k -c /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "admin123"}' http://localhost:8080/auth/login > /dev/null
echo "Admin cookie saved."
echo ""

echo "--- Obtaining participant JWT cookie ---"
curlie -k -c /tmp/curlie_participant.txt -H "Content-Type: application/json" -d '{"firstName": "Jack", "lastName": "Tester", "email": "jack.tester@example.com"}' http://localhost:8080/auth/participant > /dev/null
echo "Participant cookie saved."
echo ""

# ──────────────────────────────────────────
# Seed sample rooms for GET tests
# ──────────────────────────────────────────
echo "--- Seeding test rooms ---"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "Workshop Alpha"}' http://localhost:8080/rooms > /dev/null 2>&1
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "Conference Hall"}' http://localhost:8080/rooms > /dev/null 2>&1
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "Salle principale"}' http://localhost:8080/rooms > /dev/null 2>&1
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "LAtelier"}' http://localhost:8080/rooms > /dev/null 2>&1
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "Board Room"}' http://localhost:8080/rooms > /dev/null 2>&1
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "Meeting Room A"}' http://localhost:8080/rooms > /dev/null 2>&1
echo "Done seeding."
echo ""

# ══════════════════════════════════════════
#  200 — DEFAULT PAGINATION
# ══════════════════════════════════════════
echo "==========  200 — DEFAULT PAGINATION  =========="
echo "--- Test 1: GET /rooms with defaults (page=1, size=10) ---"
echo "    Expect: 200, data array with up to 6 rooms, meta.total=6, meta.page=1, meta.size=10"
curlie -k -b /tmp/curlie_admin.txt http://localhost:8080/rooms
echo ""

# ══════════════════════════════════════════
#  200 — CUSTOM PAGINATION
# ══════════════════════════════════════════
echo "==========  200 — CUSTOM PAGINATION  =========="
echo "--- Test 2: GET /rooms?page=2&size=3 ---"
echo "    Expect: 200, 3 rooms, meta.page=2, meta.size=3, meta.total=6"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?page=2&size=3'
echo ""

echo "--- Test 3: GET /rooms?page=1&size=2 ---"
echo "    Expect: 200, 2 rooms, meta.page=1, meta.size=2"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?page=1&size=2'
echo ""

echo "--- Test 4: GET /rooms?size=1 ---"
echo "    Expect: 200, 1 room, meta.size=1"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?size=1'
echo ""

# ══════════════════════════════════════════
#  200 — NAME FILTER (MATCH)
# ══════════════════════════════════════════
echo "==========  200 — NAME FILTER  =========="
echo "--- Test 5: GET /rooms?searchByName=Alpha ---"
echo "    Expect: 200, 1 room named 'Workshop Alpha', meta.total=1"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?searchByName=Alpha'
echo ""

echo "--- Test 6: GET /rooms?searchByName=Room ---"
echo "    Expect: 200, 2 rooms (Board Room, Meeting Room A), meta.total=2"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?searchByName=Room'
echo ""

echo "--- Test 7: GET /rooms?searchByName=main (case-insensitive via ILIKE) ---"
echo "    Expect: 200, at least 1 room (Salle principale), meta.total >= 1"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?searchByName=main'
echo ""

echo "--- Test 8: GET /rooms?searchByName=Salle&page=1&size=1 ---"
echo "    Expect: 200, 1 room, meta.total=1, meta.page=1, meta.size=1"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?searchByName=Salle&page=1&size=1'
echo ""

# ══════════════════════════════════════════
#  200 — NAME FILTER (NO MATCH)
# ══════════════════════════════════════════
echo "==========  200 — NAME FILTER (NO MATCH)  =========="
echo "--- Test 9: GET /rooms?searchByName=Nonexistent ---"
echo "    Expect: 200, empty data array, meta.total=0"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?searchByName=Nonexistent'
echo ""

# ══════════════════════════════════════════
#  200 — PAGE BEYOND DATA
# ══════════════════════════════════════════
echo "==========  200 — PAGE BEYOND DATA  =========="
echo "--- Test 10: GET /rooms?page=999&size=10 ---"
echo "     Expect: 200, empty data array, meta.total=6, meta.page=999"
curlie -k -b /tmp/curlie_admin.txt 'http://localhost:8080/rooms?page=999&size=10'
echo ""

# ══════════════════════════════════════════
#  200 — PARTICIPANT ACCESS
# ══════════════════════════════════════════
echo "==========  200 — PARTICIPANT ACCESS  =========="
echo "--- Test 11: GET /rooms with participant JWT (any authenticated user) ---"
echo "     Expect: 200, same data as admin"
curlie -k -b /tmp/curlie_participant.txt 'http://localhost:8080/rooms'
echo ""

# ══════════════════════════════════════════
#  401 — UNAUTHORIZED
# ══════════════════════════════════════════
echo "==========  401 — UNAUTHORIZED  =========="
echo "--- Test 12: GET /rooms without JWT cookie ---"
echo "     Expect: 401, {status, error, message}"
curlie -k http://localhost:8080/rooms
echo ""

echo "--- Test 13: GET /rooms with invalid JWT cookie ---"
echo "     Expect: 401, {status, error, message}"
curlie -k -b "jwt=invalid-token-that-will-be-rejected" http://localhost:8080/rooms
echo ""

# ══════════════════════════════════════════
#  DONE
# ══════════════════════════════════════════
echo "==========  DONE  =========="
echo ""

# Cleanup cookie jars
rm -f /tmp/curlie_admin.txt /tmp/curlie_participant.txt
