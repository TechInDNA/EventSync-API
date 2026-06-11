#!/bin/bash

echo "Testing GET /rooms/{id}"
echo ""

echo "--- Obtaining admin JWT cookie ---"
curlie -k -c /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "admin123"}' http://localhost:8080/auth/login > /dev/null
echo "Admin cookie saved."
echo ""

echo "--- Obtaining participant JWT cookie ---"
curlie -k -c /tmp/curlie_participant.txt -H "Content-Type: application/json" -d '{"firstName": "Jack", "lastName": "Tester", "email": "jack.tester@example.com"}' http://localhost:8080/auth/participant > /dev/null
echo "Participant cookie saved."
echo ""

echo "--- Seeding test rooms ---"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "RoomById Test"}' http://localhost:8080/rooms
echo ""

echo "==========  200 — ROOM FOUND  =========="
echo "--- Test 1: GET /rooms/{id} with existing room ---"
echo "    Expect: 200, room object with id and name"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "RoomById Test"}' http://localhost:8080/rooms | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | xargs -I {} curlie -k -b /tmp/curlie_admin.txt "http://localhost:8080/rooms/{}"
echo ""

echo "==========  404 — ROOM NOT FOUND  =========="
echo "--- Test 2: GET /rooms/{id} with nonexistent UUID ---"
echo "    Expect: 404, error object"
curlie -k -b /tmp/curlie_admin.txt "http://localhost:8080/rooms/00000000-0000-0000-0000-000000000000"
echo ""

echo "==========  400 — INVALID UUID FORMAT  =========="
echo "--- Test 3: GET /rooms/{id} with malformed UUID ---"
echo "    Expect: 400, error object"
curlie -k -b /tmp/curlie_admin.txt "http://localhost:8080/rooms/not-a-uuid"
echo ""

echo "--- Test 4: GET /rooms/{id} with short string ---"
echo "    Expect: 400, error object"
curlie -k -b /tmp/curlie_admin.txt "http://localhost:8080/rooms/abc"
echo ""

echo "--- Test 5: GET /rooms/{id} with numeric string ---"
echo "    Expect: 400, error object"
curlie -k -b /tmp/curlie_admin.txt "http://localhost:8080/rooms/12345"
echo ""

echo "==========  200 — PARTICIPANT ACCESS  =========="
echo "--- Test 6: GET /rooms/{id} with participant JWT ---"
echo "    Expect: 200, same room data as admin"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "RoomById Test"}' http://localhost:8080/rooms | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | xargs -I {} curlie -k -b /tmp/curlie_participant.txt "http://localhost:8080/rooms/{}"
echo ""

echo "==========  401 — UNAUTHENTICATED  =========="
echo "--- Test 7: GET /rooms/{id} without JWT ---"
echo "    Expect: 401, error object"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "RoomById Test"}' http://localhost:8080/rooms | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | xargs -I {} curlie -k "http://localhost:8080/rooms/{}"
echo ""

echo "--- Test 8: GET /rooms/{id} with invalid JWT ---"
echo "    Expect: 401, error object"
curlie -k -b /tmp/curlie_admin.txt -H "Content-Type: application/json" -d '{"name": "RoomById Test"}' http://localhost:8080/rooms | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | xargs -I {} curlie -k -b "jwt=invalid-token" "http://localhost:8080/rooms/{}"
echo ""

echo "==========  DONE  =========="
echo ""

rm -f /tmp/curlie_admin.txt /tmp/curlie_participant.txt
