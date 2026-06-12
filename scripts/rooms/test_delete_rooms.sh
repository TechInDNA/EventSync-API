#!/bin/bash
# Test: DELETE /rooms/{id} (requires ADMIN auth via JWT cookie)
# Depends on seed data: 37731480-3994-4ed0-8ee6-9b4f7811ea84
# Admin: admin@eventsync.com / admin123

COOKIE_JAR="/tmp/eventsync-cookies.txt"
SEED_ID="37731480-3994-4ed0-8ee6-9b4f7811ea84"
FAKE_ID="00000000-0000-0000-0000-000000000000"

echo "=== AUTH ==="
echo ""
echo "=== [200] POST /auth/login — retrieve JWT cookie ==="
curlie -c "$COOKIE_JAR" POST :8080/auth/login email=admin@eventsync.com password=admin123
echo ""

echo "=== SUCCESS ==="
echo ""
echo "=== [204] DELETE existing room by seed UUID ==="
curlie -b "$COOKIE_JAR" DELETE ":8080/rooms/$SEED_ID"
echo ""

echo "=== [404] DELETE same room again — already deleted ==="
curlie -b "$COOKIE_JAR" DELETE ":8080/rooms/$SEED_ID"
echo ""

echo "=== NOT FOUND ==="
echo ""
echo "=== [404] DELETE with non-existent UUID ==="
curlie -b "$COOKIE_JAR" DELETE ":8080/rooms/$FAKE_ID"
echo ""

echo "=== UNAUTHORIZED / FORBIDDEN ==="
echo ""
echo "=== [401] DELETE without JWT cookie ==="
curlie DELETE ":8080/rooms/$SEED_ID"
echo ""

echo "=== [200] POST /auth/participant — retrieve PARTICIPANT cookie ==="
curlie -c "$COOKIE_JAR" POST :8080/auth/participant firstName=Jean lastName=Dupont email=jean@test.com
echo ""

echo "=== [403] DELETE by PARTICIPANT (not ADMIN) ==="
curlie -b "$COOKIE_JAR" DELETE ":8080/rooms/$SEED_ID"
echo ""

# re-auth as admin for remaining tests
echo "=== [200] POST /auth/login — re-auth as ADMIN ==="
curlie -c "$COOKIE_JAR" POST :8080/auth/login email=admin@eventsync.com password=admin123
echo ""

echo "=== INVALID UUID ==="
echo ""
echo "=== [400] DELETE with malformed UUID ==="
curlie -b "$COOKIE_JAR" DELETE ':8080/rooms/not-a-uuid'
echo ""

echo "=== [400] DELETE with partial UUID (1234) ==="
curlie -b "$COOKIE_JAR" DELETE ':8080/rooms/1234'
echo ""

echo "=== VERIFICATION ==="
echo ""
echo "=== [200] GET all rooms — deleted room should be absent ==="
curlie -b "$COOKIE_JAR" GET ':8080/rooms?size=100'
echo ""

# cleanup
rm -f "$COOKIE_JAR"
