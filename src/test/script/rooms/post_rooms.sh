#!/bin/bash

echo "Testing POST /rooms"
echo ""

# ──────────────────────────────────────────
# Login to populate cookie jar
# ──────────────────────────────────────────
echo "--- Obtaining JWT cookie ---"
curlie -k -c /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "admin123"}' http://localhost:8080/auth/login > /dev/null
echo "Cookie saved."
echo ""

echo "==========  VALID CREATION (201)  =========="
echo "--- Test 1: Create room with valid name (should return 201 + room data) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "Workshop Alpha"}' http://localhost:8080/rooms

echo ""
echo "--- Test 2: Create another room (should return 201) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "Conference Hall"}' http://localhost:8080/rooms

echo ""
echo "--- Test 3: Create room with accented name (should return 201) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "Salle principale"}' http://localhost:8080/rooms

echo ""
echo "--- Test 4: Create room with apostrophe in name (should return 201) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "LAtelier"}' http://localhost:8080/rooms

echo ""
echo "==========  CONFLICT (409)  =========="
echo "--- Test 5: Re-create same name (should return 409) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "Workshop Alpha"}' http://localhost:8080/rooms

echo ""
echo "==========  UNAUTHORIZED (401)  =========="
echo "--- Test 6: No JWT cookie (should return 401) ---"
curlie -k -H "Content-Type: application/json" -d '{"name": "NoAuth Hall"}' http://localhost:8080/rooms

echo ""
echo "--- Test 7: Invalid JWT cookie (should return 401) ---"
curlie -k -b "jwt=invalid-token" -H "Content-Type: application/json" -d '{"name": "BadToken Room"}' http://localhost:8080/rooms

echo ""
echo "==========  FORBIDDEN (403)  =========="
echo "--- Test 7b: Obtain participant JWT ---"
curlie -k -c /tmp/curlie_participant.txt -H "Content-Type: application/json" -d '{"firstName": "Jack", "lastName": "Tester", "email": "jack.tester@example.com"}' http://localhost:8080/auth/participant > /dev/null
echo "Participant cookie saved."
echo ""
echo "--- Test 8: Participant tries to create a room (should return 403) ---"
curlie -k -b /tmp/curlie_participant.txt -H "Content-Type: application/json" -d '{"name": "Participant Room"}' http://localhost:8080/rooms

echo ""
echo "==========  UNPROCESSABLE ENTITY (422)  =========="
echo "--- Test 9: Empty name (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": ""}' http://localhost:8080/rooms

echo ""
echo "--- Test 10: Name with only spaces (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "   "}' http://localhost:8080/rooms

echo ""
echo "--- Test 11: Name with illegal special characters (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "<script>alert(1)</script>"}' http://localhost:8080/rooms

echo ""
echo "--- Test 12: Name exceeding 50 characters (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"name": "ThisRoomNameIsWayTooLongAndShouldBeRejectedByTheValidator"}' http://localhost:8080/rooms

echo ""
echo "--- Test 13: Name with SQL injection attempt (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d "{\"name\": \"Room' OR '1'='1\"}" http://localhost:8080/rooms

echo ""
echo "==========  BAD REQUEST (400)  =========="
echo "--- Test 14: Malformed JSON (should return 400) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{bad json}' http://localhost:8080/rooms

echo ""
echo "==========  UNPROCESSABLE ENTITY (422) - MISSING NAME  =========="
echo "--- Test 15: Empty body, name missing (should return 422) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{}' http://localhost:8080/rooms

echo ""
echo "--- Test 16: Wrong field name (should return 422 since name is missing) ---"
curlie -k -b /tmp/curlie_cookies.txt -H "Content-Type: application/json" -d '{"roomName": "My Room"}' http://localhost:8080/rooms

echo ""
echo "==========  DONE  =========="

# Cleanup
rm -f /tmp/curlie_cookies.txt /tmp/curlie_participant.txt
