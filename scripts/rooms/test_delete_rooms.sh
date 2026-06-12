#!/bin/bash
# Test: DELETE /rooms/{id}
# Depends on seed data: 37731480-3994-4ed0-8ee6-9b4f7811ea84

SEED_ID="37731480-3994-4ed0-8ee6-9b4f7811ea84"
FAKE_ID="00000000-0000-0000-0000-000000000000"

echo "=== SUCCESS ==="
echo ""
echo "=== [204] DELETE existing room by seed UUID ==="
curlie DELETE ":8080/rooms/$SEED_ID"
echo ""

echo "=== [404] DELETE same room again — already deleted ==="
curlie DELETE ":8080/rooms/$SEED_ID"
echo ""

echo "=== NOT FOUND ==="
echo ""
echo "=== [404] DELETE with non-existent UUID ==="
curlie DELETE ":8080/rooms/$FAKE_ID"
echo ""

echo "=== INVALID UUID ==="
echo ""
echo "=== [400] DELETE with malformed UUID (not-a-uuid) ==="
curlie DELETE ':8080/rooms/not-a-uuid'
echo ""

echo "=== [400] DELETE with partial UUID (1234) ==="
curlie DELETE ':8080/rooms/1234'
echo ""

echo "=== VERIFICATION ==="
echo ""
echo "=== [200] GET all rooms — deleted room should be absent ==="
curlie GET ':8080/rooms?size=100'
echo ""
