#!/bin/bash

ALICE_UUID="c0c59fa0-be24-4994-bc6c-884dbb1953d0"
BOB_UUID="c0c59fa0-be24-4994-bc6c-884dbb1953d1"
UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo ""
echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== [200] speaker with external links + sessions (Bob) — expect: full detail ==="
curlie GET ":8080/speakers/$BOB_UUID"

echo ""
echo ""

echo "=== [200] speaker without external links / sessions (Alice) — expect: null links + sessions ==="
curlie GET ":8080/speakers/$ALICE_UUID"

echo ""
echo ""
echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] UUID not found — expect: NotFoundException ==="
curlie GET ":8080/speakers/$UNKNOWN_UUID"

echo ""
echo ""
echo "=========================================="
echo "  TYPE MISMATCH (400)"
echo "=========================================="
echo ""

echo "=== [400] invalid UUID format (not-a-uuid) === MethodArgumentTypeMismatchException ==="
curlie GET ":8080/speakers/not-a-uuid"

echo ""
echo ""

echo "=== [400] invalid UUID format (12345) — numeric string ==="
curlie GET ":8080/speakers/12345"

echo ""
echo ""

echo "=== [400] invalid UUID format (uuid) — literal string ==="
curlie GET ":8080/speakers/uuid"

echo ""
echo ""
echo "=========================================="
echo "  EDGE CASES"
echo "=========================================="
echo ""

echo "=== [404] UUID all zeros — valid format but no match ==="
curlie GET ":8080/speakers/00000000-0000-0000-0000-000000000000"
