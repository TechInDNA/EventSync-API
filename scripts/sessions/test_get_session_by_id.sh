#!/bin/bash

# ──────────────────────────────────────────────
#  GET /sessions/{id} — Test script
#  Covers: full detail (speakers + questions + upvotes),
#          sparse case (no speakers / no questions),
#          404 not-found, and 400 invalid UUID.
# ──────────────────────────────────────────────

# Session #1 — has 2 speakers + 4 questions (with upvotes)
SESSION_FULL="7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d"

# Session #2 — has 1 speaker, 0 questions
SESSION_SPEAKERS_ONLY="8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e"

# Session #3 — no speakers, no questions
SESSION_EMPTY="9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f"

UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"

echo ""
echo "=========================================="
echo "  SUCCESS (200) — full detail"
echo "=========================================="
echo ""

echo "=== [200] session with 2 speakers + 4 questions (full detail) ==="
curlie GET ":8080/sessions/${SESSION_FULL}"

echo ""
echo ""
echo "=== [200] field-by-field check — expect: speakers[2], questions[4], upvotes per question ==="
curlie GET ":8080/sessions/${SESSION_FULL}" | head -100

echo ""
echo ""
echo "=========================================="
echo "  SUCCESS (200) — sparse cases"
echo "=========================================="
echo ""

echo "=== [200] session with speakers only — expect: speakers[1], questions null ==="
curlie GET ":8080/sessions/${SESSION_SPEAKERS_ONLY}"

echo ""
echo ""
echo "=== [200] empty session — expect: speakers null, questions null ==="
curlie GET ":8080/sessions/${SESSION_EMPTY}"

echo ""
echo ""
echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] UUID not found — expect: Session <uuid> not found ==="
curlie GET ":8080/sessions/${UNKNOWN_UUID}"

echo ""
echo ""
echo "=========================================="
echo "  TYPE MISMATCH (400)"
echo "=========================================="
echo ""

echo "=== [400] invalid UUID format (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie GET ":8080/sessions/not-a-uuid"

echo ""
echo ""
echo "=== [400] invalid UUID format (12345) — numeric string ==="
curlie GET ":8080/sessions/12345"

echo ""
echo ""
echo "=== [400] invalid UUID format (uuid) — literal string ==="
curlie GET ":8080/sessions/uuid"

echo ""
echo ""
echo "=========================================="
echo "  EDGE CASES"
echo "=========================================="
echo ""

echo "=== [404] UUID all zeros — valid format but no match ==="
curlie GET ":8080/sessions/00000000-0000-0000-0000-000000000000"

echo ""
echo ""
echo "=========================================="
echo "  AUTH (GET is public)"
echo "=========================================="
echo ""

echo "=== [200] no auth required — public endpoint ==="
curlie -X GET ":8080/sessions/${SESSION_FULL}"
