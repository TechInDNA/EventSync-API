#!/bin/bash

SPEAKER_WITH_LINKS="95f04aa3-431f-453a-8067-137a0e2fd718"
SPEAKER_WITHOUT_LINKS="f94b5d76-9a2d-47cf-8caf-399a384f7f79"
EXISTING_LINK_ID="73b5c723-5cbd-4a29-aad3-20d54cba6615"
UNKNOWN_UUID="00000000-0000-0000-0000-000000000000"
NAME_OF_EXISTING_LINK="Twitter"

echo "=== AUTH SETUP — Login admin ===="
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  AUTH ERRORS (401)"
echo "=========================================="
echo ""

echo "=== Test #1: [401] no JWT cookie ==="
curlie -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo "=== Test #2: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #3: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #4: [400] invalid speaker UUID (not-a-uuid) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/not-a-uuid/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo "=== Test #5: [400] invalid speaker UUID (12345) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/12345/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo "=== Test #6: [400] invalid externalLinkId (not-a-uuid) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=not-a-uuid"

echo ""
echo "=== Test #7: [400] invalid externalLinkId (12345) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=12345"

echo ""
echo "=== Test #8: [400] missing externalLinkId query param ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link"

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== Test #9: [404] speaker UUID does not exist ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$UNKNOWN_UUID/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo "=== Test #10: [404] externalLinkId does not exist ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$UNKNOWN_UUID"

echo ""
echo "=== Test #11: [404] speaker has no external links ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITHOUT_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (204)"
echo "=========================================="
echo ""

echo "=== Test #12: [204] delete existing $NAME_OF_EXISTING_LINK link from speaker with multiple links ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

echo ""
echo "=== Test #13: [404] delete same link again (now gone) ==="
curlie -b /tmp/eventsync_admin.txt -X DELETE ":8080/speakers/$SPEAKER_WITH_LINKS/external-link?externalLinkId=$EXISTING_LINK_ID"

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
