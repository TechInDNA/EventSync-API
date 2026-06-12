#!/bin/bash
# Setup: create room table and seed with a room to delete
# Usage: ./scripts/rooms/setup_rooms.sh

set -euo pipefail

ROOMS_DIR="$(cd "$(dirname "$0")/../../src/main/resources/db/rooms" && pwd)"

echo "=== Creating room schema ==="
psql "$DATABASE_URL" -f "$ROOMS_DIR/rooms_schema.sql"

echo "=== Seeding room data ==="
psql "$DATABASE_URL" -f "$ROOMS_DIR/delete_room_data.sql"

echo "=== Done ==="
echo "Room UUID: 37731480-3994-4ed0-8ee6-9b4f7811ea84"
