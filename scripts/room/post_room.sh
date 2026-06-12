#!/bin/bash
echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_jar.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo ""
echo "=== SUCCESS (201) ==="
echo ""

echo "=== [201] POST /rooms with valid name ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"Conference A"}'

echo ""
echo ""
echo "=== CONFLICT (409) ==="
echo ""

echo "=== [409] POST /rooms with duplicate name ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"Conference A"}'

echo ""
echo ""
echo "=== VALIDATION ERRORS (422) ==="
echo ""

echo "=== [422] empty name ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":""}'

echo ""
echo "=== [422] blank name (spaces only) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"   "}'

echo ""
echo "=== [422] null name (missing field) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{}'

echo ""
echo "=== [422] null name (explicit null) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":null}'

echo ""
echo "=== [422] invalid characters (XSS) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"<script>alert(1)</script>"}'

echo ""
echo "=== [422] name too long (>50 chars) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"ThisRoomNameIsWayTooLongAndShouldBeRejectedByTheValidator"}'

echo ""
echo "=== [422] name starting with non-letter (format) ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"-invalid-start"}'

echo ""
echo ""
echo "=== MALFORMED REQUEST (400) ==="
echo ""

echo "=== [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_jar.txt -X POST :8080/rooms -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""
echo "=== UNAUTHORIZED (401) ==="
echo ""

echo "=== [401] no JWT cookie ==="
curlie -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"No Auth Room"}'

echo ""
echo "=== [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/rooms -H 'Content-Type: application/json' -d '{"name":"Bad Token Room"}'

rm -f /tmp/eventsync_jar.txt
