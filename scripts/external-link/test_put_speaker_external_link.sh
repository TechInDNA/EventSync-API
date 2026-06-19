#!/bin/bash

# Test script for PUT /speakers/{id}/external-link
# Uses the seed data from db/speaker/put_speaker_external_link_data.sql

SPEAKER_WITH_LINKS="b8a86aa8-5174-4843-b247-f2e09ccd3573"
SPEAKER_WITHOUT_LINKS="965840a0-bf4e-46ae-a6b3-076e75f376dc"
UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"

echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin_ext.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant_ext.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== [200] PUT /speakers/{id}/external-link — update Twitter name and url ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/new_handle_put"}'

echo ""
echo ""
echo "=== [200] PUT /speakers/{id}/external-link — update GitHub only url (keep name) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=GitHub -H 'Content-Type: application/json' -d '{"name":"GitHub","url":"https://github.com/new_handle_put"}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""
echo "=== [400] invalid speaker UUID format (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/not-a-uuid/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/test"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] PUT /speakers/{id}/external-link with unknown speaker UUID ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$UNKNOWN_UUID/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/ghost"}'

echo ""
echo ""
echo "=== [404] PUT /speakers/{id}/external-link with known speaker but unknown urlName ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=NonExistentLink -H 'Content-Type: application/json' -d '{"name":"NonExistentLink","url":"https://example.com/new"}'

echo ""
echo ""
echo "=== [404] PUT /speakers/{id}/external-link on speaker with no external links ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link?urlName=AnyLink -H 'Content-Type: application/json' -d '{"name":"AnyLink","url":"https://example.com/any"}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "--- Setup: reset Twitter link to original URL for conflict test ---"
curlie -s -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/linked_put"}' > /dev/null

echo ""
echo "=== [409] PUT /speakers/{id}/external-link with url that already belongs to another link ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://github.com/linked_put"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — name"
echo "=========================================="
echo ""

echo "=== [422] name empty ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"","url":"https://example.com/test"}'

echo ""
echo ""
echo "=== [422] name blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"   ","url":"https://example.com/test"}'

echo ""
echo ""
echo "=== [422] name null (missing field) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"url":"https://example.com/test"}'

echo ""
echo ""
echo "=== [422] name null (explicit null) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":null,"url":"https://example.com/test"}'

echo ""
echo ""
echo "=== [422] name too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"ThisExternalLinkNameIsWayTooLongAndExceedsFiftyCharactersX","url":"https://example.com/test"}'

echo ""
echo ""
echo "=== [422] name invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"<script>alert(1)</script>","url":"https://example.com/test"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — url"
echo "=========================================="
echo ""

echo "=== [422] url empty ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":""}'

echo ""
echo ""
echo "=== [422] url null (missing field) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter"}'

echo ""
echo ""
echo "=== [422] url null (explicit null) ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":null}'

echo ""
echo ""
echo "=== [422] invalid URL format ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"not-a-valid-url"}'

echo ""
echo ""
echo "=== [422] url with invalid characters ==="
curlie -b /tmp/eventsync_admin_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://example.com/<script>alert(1)</script>"}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== [401] no JWT cookie ==="
curlie -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/unauth"}'

echo ""
echo ""
echo "=== [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/badtoken"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant_ext.txt -X PUT :8080/speakers/$SPEAKER_WITH_LINKS/external-link?urlName=Twitter -H 'Content-Type: application/json' -d '{"name":"Twitter","url":"https://twitter.com/particant"}'

echo ""
echo ""

rm -f /tmp/eventsync_admin_ext.txt /tmp/eventsync_participant_ext.txt
