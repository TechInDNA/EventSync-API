#!/bin/bash

# ──────────────────────────────────────────────
#  GET /sessions/{id}/questions — Test script
#  Tests all query params: sort, page, size, title
# ──────────────────────────────────────────────

SESSION_ID="4089df22-c66d-4c9c-bfa9-8bcec8d2e943"
NONEXISTENT_SESSION="00000000-0000-0000-0000-000000000001"

echo ""
echo "=========================================="
echo "  DEFAULT & PAGINATION"
echo "=========================================="
echo ""

echo "=== [200] default pagination (page=1, size=20, sort=upvotes) ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions"

echo ""
echo "=== [200] custom pagination (page=1, size=5) ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=1&size=5"

echo ""
echo "=== [200] page=2, size=5 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=2&size=5"

echo ""
echo "=== [200] size=100 (all questions) ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=1&size=100"

echo ""
echo "=== [200] page out of range — expect: 0 questions ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=999&size=10"

echo ""
echo ""
echo "=========================================="
echo "  SORT PARAMETERS"
echo "=========================================="
echo ""

echo "=== [200] sort=upvotes (default) — most upvoted first ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?sort=upvotes"

echo ""
echo "=== [200] sort=createdAt — oldest first ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?sort=createdAt"

echo ""
echo ""
echo "=========================================="
echo "  TITLE FILTER"
echo "=========================================="
echo ""

echo "=== [200] title=Spring — partial match, expect: questions with 'Spring' in title ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Spring"

echo ""
echo "=== [200] title=Dependency — partial match, expect: 1 question (Dependency Injection?) ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Dependency"

echo ""
echo "=== [200] title=dependency — case-insensitive ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=dependency"

echo ""
echo "=== [200] title=DI — short partial match ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=DI"

echo ""
echo "=== [200] title=Nonexistent — expect: 0 questions ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Nonexistent"

echo ""
echo "=== [200] title= (empty) — treated as no filter, expect: all questions ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title="

echo ""
echo ""
echo "=========================================="
echo "  COMBINED FILTERS"
echo "=========================================="
echo ""

echo "=== [200] title=Spring & sort=createdAt ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Spring&sort=createdAt"

echo ""
echo "=== [200] title=Spring & page=1 & size=2 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Spring&page=1&size=2"

echo ""
echo "=== [200] title=DI & sort=upvotes & page=1 & size=10 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=DI&sort=upvotes&page=1&size=10"

echo ""
echo ""
echo "=========================================="
echo "  NEGATIVE TESTS"
echo "=========================================="
echo ""

echo "=== [404] non-existent session ID ==="
curlie GET ":8080/sessions/${NONEXISTENT_SESSION}/questions"

echo ""
echo "=== [422] sort=invalid (not 'upvotes' or 'createdAt') ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?sort=invalid"

echo ""
echo ""
echo "=========================================="
echo "  EDGE CASES"
echo "=========================================="
echo ""

echo "=== [200] page=0 — normalised to page=1 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=0"

echo ""
echo "=== [200] page=-5 — normalised to page=1 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=-5"

echo ""
echo "=== [200] size=0 — normalised to size=20 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?size=0"

echo ""
echo "=== [200] size=-1 — normalised to size=20 ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?size=-1"

echo ""
echo "=== [200] page=1&size=0 — normalised, expect: 20 questions ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=1&size=0"

echo ""
echo "=== [400] page=abc — invalid int ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?page=abc"

echo ""
echo "=== [400] size=abc — invalid int ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?size=abc"

echo ""
echo "=== [200] title=a — single char allowed ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=a"

echo ""
echo "=== [422] title=Title! — invalid char ! rejected ==="
curlie GET ":8080/sessions/${SESSION_ID}/questions?title=Title!"

echo ""
echo ""
echo "=========================================="
echo "  AUTH (GET is public)"
echo "=========================================="
echo ""

echo "=== [200] no auth required — public endpoint ==="
curlie -X GET ":8080/sessions/${SESSION_ID}/questions"
