#!/bin/bash

SPEAKER_WITH_LINKS="a34db822-3568-42e5-b94c-62eb45a996af"
SPEAKER_WITHOUT_LINKS="6d2dfa8c-25bc-4a3c-9ba1-a316e7a3c5a0"
UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"

echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (201)"
echo "=========================================="
echo ""

echo "=== Test #1: [201] POST /speakers/{id}/external-link — add link to speaker with existing links ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITH_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"LinkedIn","url":"https://linkedin.com/in/jean_lie"}'

echo ""
echo ""
echo "=== Test #2: [201] POST /speakers/{id}/external-link — add first link to speaker with no links ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/solo_nu"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #3: [404] POST /speakers/{id}/external-link with non-existent UUID ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$UNKNOWN_UUID/external-link -H 'Content-Type: application/json' -d '{"name":"Nowhere","url":"https://nowhere.example.com"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #4: [409] POST /speakers/{id}/external-link with URL that already exists ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Twitter Dupe","url":"https://twitter.com/solo_nu"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — name"
echo "=========================================="
echo ""

echo "=== Test #5: [422] name empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"","url":"https://example.com/valid"}'

echo ""
echo "=== Test #6: [422] name null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"url":"https://example.com/valid"}'

echo ""
echo "=== Test #7: [422] name null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":null,"url":"https://example.com/valid"}'

echo ""
echo "=== Test #8: [422] name invalid characters (digits) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"MyLink123","url":"https://example.com/valid"}'

echo ""
echo "=== Test #9: [422] name too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"ThisExternalLinkNameIsWayTooLongAndShouldBeRejectedByTheValidator","url":"https://example.com/valid"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — url"
echo "=========================================="
echo ""

echo "=== Test #10: [422] url empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":""}'

echo ""
echo "=== Test #11: [422] url null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Valid"}'

echo ""
echo "=== Test #12: [422] url null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":null}'

echo ""
echo "=== Test #13: [422] url invalid format (not an http url) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":"not-a-url"}'

echo ""
echo "=== Test #14: [422] url invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":"https://example.com/<script>alert(1)</script>"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #15: [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""
echo "=== Test #16: [400] invalid UUID (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/not-a-uuid/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":"https://example.com/valid"}'

echo ""
echo ""
echo "=== Test #17: [400] invalid UUID (numeric 12345) — not parseable as UUID ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers/12345/external-link -H 'Content-Type: application/json' -d '{"name":"Valid","url":"https://example.com/valid"}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #18: [401] no JWT cookie ==="
curlie -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"No Auth","url":"https://example.com/no-auth"}'

echo ""
echo "=== Test #19: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Bad Token","url":"https://example.com/bad-token"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #20: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X POST :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link -H 'Content-Type: application/json' -d '{"name":"Participant","url":"https://example.com/participant"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
