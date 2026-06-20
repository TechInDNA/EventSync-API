#!/bin/bash

# Session #1 — 2 speakers + 4 questions (with upvotes)
SESSION_FULL="7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d"

# Session #2 — 1 speaker, 0 questions
SESSION_SPEAKERS_ONLY="8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e"

# Session #3 — no speakers, no questions
SESSION_EMPTY="9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f"

UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo ""
echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== [200] session with 2 speakers + 4 questions (full detail) ==="
curlie GET ":8080/sessions/$SESSION_FULL"

echo ""
echo ""

echo "=== [200] session with 1 speaker, no questions === (speakers array, questions null)"
curlie GET ":8080/sessions/$SESSION_SPEAKERS_ONLY"

echo ""
echo ""

echo "=== [200] session with no speakers, no questions === (speakers null, questions null)"
curlie GET ":8080/sessions/$SESSION_EMPTY"

echo ""
echo ""
echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] UUID not found === (Session <uuid> not found)"
curlie GET ":8080/sessions/$UNKNOWN_UUID"

echo ""
echo ""
echo "=========================================="
echo "  TYPE MISMATCH (400)"
echo "=========================================="
echo ""

echo "=== [400] invalid UUID (not-a-uuid) ==="
curlie GET ":8080/sessions/not-a-uuid"

echo ""
echo ""

echo "=== [400] invalid UUID (12345) ==="
curlie GET ":8080/sessions/12345"

echo ""
echo ""

echo "=== [400] invalid UUID (uuid) ==="
curlie GET ":8080/sessions/uuid"

echo ""
echo ""
echo "=========================================="
echo "  EDGE CASES"
echo "=========================================="
echo ""

echo "=== [404] UUID all zeros — valid format, no match ==="
curlie GET ":8080/sessions/00000000-0000-0000-0000-000000000000"

echo ""
echo ""
echo "=========================================="
echo "  AUTH (GET is public)"
echo "=========================================="
echo ""

echo "=== [200] no auth required — public endpoint ==="
curlie -X GET ":8080/sessions/$SESSION_FULL"

echo ""
echo ""
