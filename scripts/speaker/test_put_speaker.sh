#!/bin/bash

SEED_UUID="c0c59fa0-be24-4994-bc6c-884dbb1953a0"
UNKNOWN_UUID="077b8eae-64ce-4bcf-b339-4dee8d0c0ca0"

echo "=== AUTH SETUP — Login admin ==="
curlie -s -c /tmp/eventsync_admin_jar.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ==="
curlie -s -c /tmp/eventsync_participant_jar.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (200)"
echo "=========================================="
echo ""

echo "=== [200] PUT /speakers/{id} with all fields — change everything ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"Renamed","lastName":"Speaker","email":"renamed.speaker@example.com","profilePicture":"https://example.com/avatars/renamed.jpg","bio":"Updated bio after renaming."}'

echo ""
echo ""

echo "=== [200] PUT /speakers/{id} update only name and email, keep bio/picture ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"Still","lastName":"Speaker","email":"still.speaker@example.com","profilePicture":"https://example.com/avatars/still.jpg","bio":"Still the same bio."}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo "=== [400] invalid UUID format (not-a-uuid) — MethodArgumentTypeMismatchException ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/not-a-uuid -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo ""

echo "=========================================="
echo "  NOT FOUND (404)"
echo "=========================================="
echo ""

echo "=== [404] PUT /speakers/{id} with unknown UUID ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$UNKNOWN_UUID -H 'Content-Type: application/json' -d '{"firstName":"Ghost","lastName":"Speaker","email":"ghost@example.com","profilePicture":"https://example.com/avatars/ghost.jpg","bio":"Does not exist."}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "--- Setup: create a second speaker for duplicate-email test ---"
SECOND_EMAIL="duplicate.conflict@example.com"
curlie -s -b /tmp/eventsync_admin_jar.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d "{\"firstName\":\"Second\",\"lastName\":\"Speaker\",\"email\":\"$SECOND_EMAIL\",\"profilePicture\":\"https://example.com/avatars/second.jpg\",\"bio\":\"Second speaker to test conflict.\"}" > /dev/null

echo "=== [409] PUT /speakers/{id} with email that already exists (unique constraint) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d "{\"firstName\":\"Conflict\",\"lastName\":\"Test\",\"email\":\"$SECOND_EMAIL\",\"profilePicture\":\"https://example.com/avatars/conflict.jpg\",\"bio\":\"This should conflict.\"}"

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — firstName"
echo "=========================================="
echo ""

echo "=== [422] firstName empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"   ","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":null,"lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"<script>alert(1)</script>","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"JohnathanAlexanderBenjaminChristopherDavidMatthewOX","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] firstName starting with non-letter ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"-john","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — lastName"
echo "=========================================="
echo ""

echo "=== [422] lastName empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] lastName blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"   ","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] lastName null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] lastName null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":null,"email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] lastName too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"VonSchwarzeneggerLongLastNameTestHereExtraLongggXXXXX","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — email"
echo "=========================================="
echo ""

echo "=== [422] email empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] email null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] email null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":null,"profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] invalid email format (no domain) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"not-an-email","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] invalid email format (no @) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john.doe","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] email too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"johnathan.verylongemailaddress.extra.long@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo "=== [422] email with invalid characters ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john<>doe@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bio"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — profilePicture"
echo "=========================================="
echo ""

echo "=== [422] profilePicture empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"","bio":"Bio"}'

echo ""
echo "=== [422] profilePicture invalid URL format ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"not-a-url","bio":"Bio"}'

echo ""
echo "=== [422] profilePicture null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","bio":"Bio"}'

echo ""
echo "=== [422] profilePicture null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":null,"bio":"Bio"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — bio"
echo "=========================================="
echo ""

echo "=== [422] bio too long (>1000 chars) ==="
LONG_BIO=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"test@example.com\",\"profilePicture\":\"https://example.com/pic.jpg\",\"bio\":\"$LONG_BIO\"}"

echo ""
echo "=== [422] bio with invalid characters ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Hello <world>"}'

echo ""
echo "=== [422] bio null (missing field) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== [422] bio null (explicit null) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":null}'

echo ""
echo "=== [422] bio empty ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":""}'

echo ""
echo "=== [422] bio blank (spaces only) ==="
curlie -b /tmp/eventsync_admin_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"   "}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== [401] no JWT cookie ==="
curlie -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"NoAuth","lastName":"Speaker","email":"noauth@example.com","profilePicture":"https://example.com/pic.jpg","bio":"No auth"}'

echo ""
echo "=== [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"BadToken","lastName":"Speaker","email":"badtoken@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Bad token"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant_jar.txt -X PUT :8080/speakers/$SEED_UUID -H 'Content-Type: application/json' -d '{"firstName":"Participant","lastName":"Try","email":"participant@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Participant cannot update"}'

echo ""

rm -f /tmp/eventsync_admin_jar.txt /tmp/eventsync_participant_jar.txt
