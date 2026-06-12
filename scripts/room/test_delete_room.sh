#!/bin/bash
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_jar.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== [204] Delete existing room (seed room) ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/37731480-3994-4ed0-8ee6-9b4f7811ea84

echo ""
echo "=== [404] Delete already-deleted room ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/37731480-3994-4ed0-8ee6-9b4f7811ea84

echo ""
echo "=== [404] Delete non-existent room (valid UUID) ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/00000000-0000-0000-0000-000000000000

echo ""
echo "=== [401] Delete without auth (no JWT cookie) ==="
curlie -X DELETE :8080/rooms/37731480-3994-4ed0-8ee6-9b4f7811ea84

echo ""
echo "=== [400] Delete with invalid UUID (not-a-uuid) ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/not-a-uuid

echo ""
echo "=== [400] Delete with invalid UUID (12345) ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/12345

echo ""
echo "=== [400] Delete with invalid UUID (too-long-with-hyphens) ==="
curlie -b /tmp/eventsync_jar.txt -X DELETE :8080/rooms/too-long-with-hyphens

rm -f /tmp/eventsync_jar.txt
