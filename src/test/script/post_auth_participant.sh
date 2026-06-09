#!/bin/bash

echo "Testing POST /auth/participant"

echo ""
echo "==========  VALID (200)  =========="
echo "--- Test 1: Register new participant (should return 200 + JWT token + participant data) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "john.doe@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 2: Re-identify existing participant (should return 200 with same data) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "john.doe@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 3: Register another participant (should return 200) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "Jane", "lastName": "Smith", "email": "jane.smith@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "==========  UNPROCESSABLE ENTITY (422) - MISSING FIELDS  =========="
echo "--- Test 4: Missing firstName (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"lastName": "Doe", "email": "missing.first@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 5: Missing lastName (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "email": "missing.last@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 6: Missing email (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 7: Empty body (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{}' http://localhost:8080/auth/participant

echo ""
echo "==========  UNPROCESSABLE ENTITY (422) - INVALID EMAIL  =========="
echo "--- Test 8: Invalid email format (no @ symbol) (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "not-an-email"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 9: Email with spaces (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "john @doe.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 10: Email without TLD (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "john@doe"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 11: Email with illegal special characters (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "Doe", "email": "<john@doe.com>"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 12: SQL injection attempt in email (should return 422) ---"
curlie -H "Content-Type: application/json" -d "{\"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john@doe.com' OR '1'='1\"}" http://localhost:8080/auth/participant

echo ""
echo "==========  UNPROCESSABLE ENTITY (422) - INVALID NAMES  =========="
echo "--- Test 13: firstName with illegal special characters (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "<script>", "lastName": "Doe", "email": "xss.name@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 14: lastName with illegal special characters (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "<script>", "email": "xss.last@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 15: firstName exceeding 50 characters (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "JohnathanAlexanderMichaelChristopherBenjaminBalackMiller", "lastName": "Doe", "email": "long.first@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "--- Test 16: lastName exceeding 50 characters (should return 422) ---"
curlie -H "Content-Type: application/json" -d '{"firstName": "John", "lastName": "MontgomeryRichardsonWellingtonHarringtonMichaelChristopherBenjaminIII", "email": "long.last@example.com"}' http://localhost:8080/auth/participant

echo ""
echo "==========  DONE  =========="
