#!/bin/bash
echo "=== GET /events — default pagination (page=1, size=10) ==="
curlie :8080/events

echo ""
echo "=== GET /events — custom page & size (page=2, size=5) ==="
curlie :8080/events?page=2&size=5

echo ""
echo "=== GET /events — size=100 (all events) ==="
curlie :8080/events?page=1&size=100

echo ""
echo "=== GET /events — page out of range ==="
curlie :8080/events?page=999&size=10

echo ""
echo "=== GET /events — title filter: Spring (matches 2 events) ==="
curlie :8080/events?title=Spring

echo ""
echo "=== GET /events — title filter: Java ==="
curlie :8080/events?title=Java

echo ""
echo "=== GET /events — title filter: Rust ==="
curlie :8080/events?title=Rust

echo ""
echo "=== GET /events — title filter: no match ==="
curlie :8080/events?title=Nonexistent

echo ""
echo "=== GET /events — location filter: Antananarivo ==="
curlie :8080/events?location=Antananarivo

echo ""
echo "=== GET /events — location filter: Fianarantsoa ==="
curlie :8080/events?location=Fianarantsoa

echo ""
echo "=== GET /events — location filter: Toamasina ==="
curlie :8080/events?location=Toamasina

echo ""
echo "=== GET /events — location filter: Mahajanga ==="
curlie :8080/events?location=Mahajanga

echo ""
echo "=== GET /events — combined title + location: Spring + Antananarivo ==="
curlie :8080/events?title=Spring&location=Antananarivo

echo ""
echo "=== GET /events — combined title + location: DevOps + Antananarivo ==="
curlie :8080/events?title=DevOps&location=Antananarivo

echo ""
echo "=== GET /events — startDate filter: from 2026-01-01 (future events only) ==="
curlie ':8080/events?startDate=2026-01-01T00:00:00Z'

echo ""
echo "=== GET /events — endDate filter: until 2025-12-31 (past events only) ==="
curlie ':8080/events?endDate=2025-12-31T23:59:59Z'

echo ""
echo "=== GET /events — date range: 2025-07-01 to 2025-09-30 ==="
curlie ':8080/events?startDate=2025-07-01T00:00:00Z&endDate=2025-09-30T23:59:59Z'

echo ""
echo "=== GET /events — date range: 2026-01-01 to 2026-06-30 ==="
curlie ':8080/events?startDate=2026-01-01T00:00:00Z&endDate=2026-06-30T23:59:59Z'

echo ""
echo "=== GET /events — isLive=true (currently running events) ==="
curlie :8080/events?isLive=true

echo ""
echo "=== GET /events — isLive=false (non-live events) ==="
curlie :8080/events?isLive=false

echo ""
echo "=== GET /events — location + date range: Antananarivo in 2025 ==="
curlie ':8080/events?location=Antananarivo&startDate=2025-01-01T00:00:00Z&endDate=2025-12-31T23:59:59Z'

echo ""
echo "=== GET /events — title + date range: Spring in 2026 ==="
curlie ':8080/events?title=Spring&startDate=2026-01-01T00:00:00Z&endDate=2026-12-31T23:59:59Z'

echo ""
echo "=== GET /events — location + isLive: Antananarivo + isLive=false ==="
curlie :8080/events?location=Antananarivo&isLive=false

echo ""
echo "=== GET /events — title + location + date range: full combo ==="
curlie ':8080/events?title=Spring&location=Antananarivo&startDate=2026-01-01T00:00:00Z&endDate=2026-06-30T23:59:59Z'

echo ""
echo "=== GET /events — single event match: title=Kubernetes ==="
curlie :8080/events?title=Kubernetes

echo ""
echo "=== GET /events — startDate only: from 2025-06-01 ==="
curlie ':8080/events?startDate=2025-06-01T00:00:00Z'

echo ""
echo "=== GET /events — endDate only: until 2025-06-01 ==="
curlie ':8080/events?endDate=2025-06-01T23:59:59Z'
