#!/bin/bash

echo "Testing POST /auth/login"
echo ""

echo "==========  VALID LOGIN (200)  =========="
echo "--- Test 1: Valid admin login (should return 200 + JWT cookie) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "admin123"}' http://localhost:8080/auth/login

echo ""
echo "==========  UNAUTHORIZED (401)  =========="
echo "--- Test 2: Invalid password (should return 401 + attempts left) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "wrongpassword"}' http://localhost:8080/auth/login

echo ""
echo "--- Test 3: Non-existent email (should return 401) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "nobody@eventsync.com", "password": "test"}' http://localhost:8080/auth/login

echo ""
echo "==========  BAD REQUEST (400)  =========="
echo "--- Test 4: Invalid email format (no @) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "not-an-email", "password": "test"}' http://localhost:8080/auth/login

echo ""
echo "--- Test 5: Missing password ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com"}' http://localhost:8080/auth/login

echo ""
echo "--- Test 6: Missing email ---"
curlie -k -H "Content-Type: application/json" -d '{"password": "test"}' http://localhost:8080/auth/login

echo ""
echo "--- Test 7: Empty body ---"
curlie -k -H "Content-Type: application/json" -d '{}' http://localhost:8080/auth/login

echo ""
echo "==========  RATE LIMITING  =========="
echo "--- Failing 4 times (401, attempts left: 4,3,2,1) ---"
for i in 4 3 2 1; do
  curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "wrong"}' http://localhost:8080/auth/login && echo ""
done

echo ""
echo "--- Test 8: 5th failure — blocked (should return 401) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "wrong"}' http://localhost:8080/auth/login

echo ""
echo "--- Test 9: 6th attempt — IP already blocked (should return 401) ---"
curlie -k -H "Content-Type: application/json" -d '{"email": "admin@eventsync.com", "password": "admin123"}' http://localhost:8080/auth/login
