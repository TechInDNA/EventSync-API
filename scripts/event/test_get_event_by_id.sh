#!/bin/bash

EVENT_WITH_SESSIONS="2b2ad0e8-e8bd-475d-9737-86a1e4081f44"
EVENT_WITHOUT_SESSIONS="d4ef0aa8-76d2-464b-8e2c-5c0cd3b3621f"
UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo ""
echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== [200] event with 3 sessions (DevCon 2026) — expect: full detail + sessions array ==="
curlie GET ":8080/events/$EVENT_WITH_SESSIONS"

echo ""
echo ""

echo "=== [200] event without sessions (Standalone Expo 2026) — expect: full detail + empty sessions ==="
curlie GET ":8080/events/$EVENT_WITHOUT_SESSIONS"

echo ""
echo ""
echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] UUID not found — expect: NotFoundException ==="
curlie GET ":8080/events/$UNKNOWN_UUID"

echo ""
echo ""
echo "=========================================="
echo "  TYPE MISMATCH (400)"
echo "=========================================="
echo ""

echo "=== [400] invalid UUID format (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie GET ":8080/events/not-a-uuid"

echo ""
echo ""

echo "=== [400] invalid UUID format (12345) — numeric string ==="
curlie GET ":8080/events/12345"

echo ""
echo ""

echo "=== [400] invalid UUID format (uuid) — literal string ==="
curlie GET ":8080/events/uuid"

echo ""
echo ""
echo "=========================================="
echo "  EDGE CASES"
echo "=========================================="
echo ""

echo "=== [404] UUID all zeros — valid format but no match ==="
curlie GET ":8080/events/00000000-0000-0000-0000-000000000000"
