#!/bin/bash

SEED_UUID="134873ba-e426-4e1b-8943-a8ee4d0951b7"
UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo ""
echo "=== SUCCESS ==="
echo ""

echo "=== [200] valid UUID — expect: room with id + name ==="
curlie GET ":8080/rooms/$SEED_UUID"

echo ""
echo ""
echo "=== NOT FOUND (404) ==="
echo ""

echo "=== [404] UUID not found — expect: NotFoundException ==="
curlie GET ":8080/rooms/$UNKNOWN_UUID"

echo ""
echo ""
echo "=== TYPE MISMATCH (400) ==="
echo ""

echo "=== [400] invalid UUID format (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie GET ":8080/rooms/not-a-uuid"

echo ""
echo "=== [400] invalid UUID format (12345) — numeric string not parseable as UUID ==="
curlie GET :8080/rooms/12345

echo ""
echo "=== [400] invalid UUID format (uuid) — literal string ==="
curlie GET :8080/rooms/uuid

echo ""
echo ""
echo "=== EDGE CASES ==="
echo ""

echo "=== [404] UUID all zeros — valid format but no match ==="
curlie GET ":8080/rooms/00000000-0000-0000-0000-000000000000"
