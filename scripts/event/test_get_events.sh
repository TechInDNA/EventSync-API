#!/bin/bash
echo "=== [200] default pagination (page=1, size=10) — expect: 10 events ==="
curlie GET :8080/events

echo ""
echo "=== [200] custom page & size (page=2, size=5) — expect: 5 events ==="
curlie GET ':8080/events?page=2&size=5'

echo ""
echo "=== [200] size=100 (all events) — expect: 22 events ==="
curlie GET ':8080/events?page=1&size=100'

echo ""
echo "=== [200] page out of range — expect: 0 events ==="
curlie GET ':8080/events?page=999&size=10'

echo ""
echo "=== [200] title=Spring — expect: 3 events (Spring Cloud, Spring Boot, Meetup Spring Boot) ==="
curlie GET :8080/events?title=Spring

echo ""
echo "=== [200] title=Java — expect: 1 event ==="
curlie GET :8080/events?title=Java

echo ""
echo "=== [200] title=Rust — expect: 1 event ==="
curlie GET :8080/events?title=Rust

echo ""
echo "=== [200] title=Nonexistent — expect: 0 events ==="
curlie GET :8080/events?title=Nonexistent

echo ""
echo "=== [200] location=Antananarivo — expect: 12 events ==="
curlie GET :8080/events?location=Antananarivo

echo ""
echo "=== [200] location=Fianarantsoa — expect: 4 events ==="
curlie GET :8080/events?location=Fianarantsoa

echo ""
echo "=== [200] location=Toamasina — expect: 4 events ==="
curlie GET :8080/events?location=Toamasina

echo ""
echo "=== [200] location=Mahajanga — expect: 2 events ==="
curlie GET :8080/events?location=Mahajanga

echo ""
echo "=== [200] title=Spring & location=Antananarivo — expect: 3 events ==="
curlie GET ':8080/events?title=Spring&location=Antananarivo'

echo ""
echo "=== [200] title=DevOps & location=Antananarivo — expect: 1 event ==="
curlie GET ':8080/events?title=DevOps&location=Antananarivo'

echo ""
echo "=== [200] startDate=2026-01-01T00:00:00Z (future only) — expect: 12 events ==="
curlie GET ':8080/events?startDate=2026-01-01T00:00:00Z'

echo ""
echo "=== [200] endDate=2025-12-31T23:59:59Z (past only) — expect: 10 events ==="
curlie GET ':8080/events?endDate=2025-12-31T23:59:59Z'

echo ""
echo "=== [200] date range 2025-07-01 → 2025-09-30 — expect: 3 events ==="
curlie GET ':8080/events?startDate=2025-07-01T00:00:00Z&endDate=2025-09-30T23:59:59Z'

echo ""
echo "=== [200] date range 2026-01-01 → 2026-06-30 — expect: 6 events ==="
curlie GET ':8080/events?startDate=2026-01-01T00:00:00Z&endDate=2026-06-30T23:59:59Z'

echo ""
echo "=== [200] isLive=true — expect: 0 events ==="
curlie GET :8080/events?isLive=true

echo ""
echo "=== [200] isLive=false — expect: 22 events ==="
curlie GET :8080/events?isLive=false

echo ""
echo "=== [200] location=Antananarivo & dates 2025 — expect: 5 events ==="
curlie GET ':8080/events?location=Antananarivo&startDate=2025-01-01T00:00:00Z&endDate=2025-12-31T23:59:59Z'

echo ""
echo "=== [200] title=Spring & dates 2026 — expect: 3 events ==="
curlie GET ':8080/events?title=Spring&startDate=2026-01-01T00:00:00Z&endDate=2026-12-31T23:59:59Z'

echo ""
echo "=== [200] location=Antananarivo & isLive=false — expect: 12 events ==="
curlie GET ':8080/events?location=Antananarivo&isLive=false'

echo ""
echo "=== [200] title=Spring & location=Antananarivo & dates 2026 H1 — expect: 3 events ==="
curlie GET ':8080/events?title=Spring&location=Antananarivo&startDate=2026-01-01T00:00:00Z&endDate=2026-06-30T23:59:59Z'

echo ""
echo "=== [200] title=Kubernetes — expect: 1 event ==="
curlie GET :8080/events?title=Kubernetes

echo ""
echo "=== [200] startDate=2025-06-01T00:00:00Z — expect: 18 events ==="
curlie GET ':8080/events?startDate=2025-06-01T00:00:00Z'

echo ""
echo "=== [200] endDate=2025-06-01T23:59:59Z — expect: 4 events ==="
curlie GET ':8080/events?endDate=2025-06-01T23:59:59Z'

echo ""
echo ""
echo "=== EXCEPTION TESTS ==="

echo ""
echo "=== [400] page=abc — invalid int for page ==="
curlie GET :8080/events?page=abc

echo ""
echo "=== [400] startDate=notadate — invalid Instant format ==="
curlie GET :8080/events?startDate=notadate

echo ""
echo "=== [400] isLive=maybe — invalid Boolean ==="
curlie GET :8080/events?isLive=maybe

echo ""
echo "=== [200] title= (empty) — treated as no filter (empty string allowed by validateSearchString) ==="
curlie GET :8080/events?title=

echo ""
echo "=== [200] title=a — single char allowed by validateSearchString ==="
curlie GET :8080/events?title=a

echo ""
echo "=== [200] title=ab — short string allowed by validateSearchString ==="
curlie GET :8080/events?title=ab

echo ""
echo "=== [422] title=Title! — invalid char ! rejected by validateSearchString ==="
curlie GET :8080/events?title=Title!

echo ""
echo "=== [200] location= (empty) — treated as no filter (empty string allowed by validateSearchString) ==="
curlie GET :8080/events?location=

echo ""
echo "=== [200] title=123 — digits allowed by validateSearchString ==="
curlie GET :8080/events?title=123

echo ""
echo ""
echo "=== EDGE CASES (graceful handling) ==="

echo ""
echo "=== [200] page=0 — normalised to page=1, expect: 10 events ==="
curlie GET :8080/events?page=0

echo ""
echo "=== [200] page=-5 — normalised to page=1, expect: 10 events ==="
curlie GET :8080/events?page=-5

echo ""
echo "=== [200] size=0 — normalised to size=10, expect: 10 events ==="
curlie GET :8080/events?size=0

echo ""
echo "=== [200] size=-1 — normalised to size=10, expect: 10 events ==="
curlie GET :8080/events?size=-1

echo ""
echo "=== [200] page=1&size=0 — normalised, expect: 10 events ==="
curlie GET ':8080/events?page=1&size=0'
