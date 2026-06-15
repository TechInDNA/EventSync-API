#!/bin/bash

echo "=== AUTH SETUP — Login admin ===" && \
curlie -s -c /tmp/eventsync_admin.txt -X POST :8080/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@eventsync.com","password":"admin123"}' > /dev/null

echo ""
echo "=== AUTH SETUP — Participant token ===" && \
curlie -s -c /tmp/eventsync_participant.txt -X POST :8080/auth/participant -H 'Content-Type: application/json' -d '{"firstName":"Jack","lastName":"Tester","email":"jack.tester@example.com"}' > /dev/null

echo ""
echo ""

echo "=========================================="
echo "  SUCCESS (201)"
echo "=========================================="
echo ""

echo "=== Test #1: [201] POST /speakers with valid input ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/avatar.jpg"}'

echo ""
echo ""
echo "=== Test #2: [201] POST /speakers with all fields (bio, profilePicture) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","profilePicture":"https://example.com/jane.jpg","bio":"Experienced software engineer and conference speaker."}'

echo ""
echo ""
echo "=== Test #3: [201] POST /speakers with external links ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"Alice","lastName":"Wonder","email":"alice.wonder@example.com","profilePicture":"https://example.com/alice.jpg","bio":"Tech lead","externalLinks":[{"name":"Twitter","url":"https://twitter.com/alice"},{"name":"GitHub","url":"https://github.com/alice"}]}'

echo ""
echo ""

echo "=========================================="
echo "  CONFLICT (409)"
echo "=========================================="
echo ""

echo "=== Test #4: [409] POST /speakers with duplicate email ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/avatar.jpg"}'

echo ""
echo ""
echo "=== Test #5: [409] POST /speakers with duplicate external link URL ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"Bob","lastName":"Test","email":"bob.test@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/bob.jpg","externalLinks":[{"name":"Twitter","url":"https://twitter.com/alice"}]}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — firstName"
echo "=========================================="
echo ""

echo "=== Test #6: [422] firstName empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #7: [422] firstName blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"   ","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #8: [422] firstName null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #9: [422] firstName null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":null,"lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #10: [422] firstName invalid characters (XSS) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"<script>alert(1)</script>","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #11: [422] firstName too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"JohnathanAlexanderBenjaminChristopherDavidMatthewOX","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #12: [422] firstName starting with non-letter ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"-john","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — lastName"
echo "=========================================="
echo ""

echo "=== Test #13: [422] lastName empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #14: [422] lastName blank (spaces only) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"   ","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #15: [422] lastName null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #16: [422] lastName null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":null,"email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #17: [422] lastName too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"VonSchwarzeneggerLongLastNameTestHereExtraLongggXXXXX","email":"test@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — email"
echo "=========================================="
echo ""

echo "=== Test #18: [422] email empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #19: [422] email null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #20: [422] email null (explicit null) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":null,"profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #21: [422] invalid email format (no domain) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"not-an-email","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #22: [422] invalid email format (no @) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john.doe","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #23: [422] email too long (>50 chars) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"johnathan.verylongemailaddress.extra.long@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #24: [422] email with invalid characters ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"john<>doe@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — profilePicture"
echo "=========================================="
echo ""

echo "=== Test #25: [422] profilePicture empty ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","bio":"Experienced speaker","profilePicture":""}'

echo ""
echo "=== Test #26: [422] profilePicture null (missing field) ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","bio":"Experienced speaker"}'

echo ""
echo "=== Test #27: [422] profilePicture invalid URL format ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","bio":"Experienced speaker","profilePicture":"not-a-url"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — bio"
echo "=========================================="
echo ""

echo "=== Test #28: [422] bio too long (>1000 chars) ==="
LONG_BIO=$(python3 -c "print('x' * 1001)")
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"test@example.com\",\"profilePicture\":\"https://example.com/pic.jpg\",\"bio\":\"$LONG_BIO\"}"

echo ""
echo "=== Test #29: [422] bio with invalid characters ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"test@example.com","profilePicture":"https://example.com/pic.jpg","bio":"Hello <world>"}'

echo ""
echo ""

echo "=========================================="
echo "  VALIDATION ERRORS (422) — external links"
echo "=========================================="
echo ""

echo "=== Test #30: [422] external link with null name ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"link.test@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/pic.jpg","externalLinks":[{"name":null,"url":"https://example.com"}]}'

echo ""
echo "=== Test #31: [422] external link with empty url ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"link.test2@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/pic.jpg","externalLinks":[{"name":"MyLink","url":""}]}'

echo ""
echo "=== Test #32: [422] external link with invalid url ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"John","lastName":"Doe","email":"link.test3@example.com","bio":"Experienced speaker","profilePicture":"https://example.com/pic.jpg","externalLinks":[{"name":"MyLink","url":"not-a-valid-url"}]}'

echo ""
echo ""

echo "=========================================="
echo "  MALFORMED REQUEST (400)"
echo "=========================================="
echo ""

echo "=== Test #33: [400] malformed JSON body — HttpMessageNotReadableException ==="
curlie -b /tmp/eventsync_admin.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{broken}'

echo ""
echo ""

echo "=========================================="
echo "  UNAUTHORIZED (401)"
echo "=========================================="
echo ""

echo "=== Test #34: [401] no JWT cookie ==="
curlie -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"NoAuth","lastName":"Speaker","email":"noauth@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo "=== Test #35: [401] invalid JWT cookie ==="
curlie -b "jwt=invalid-token" -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"BadToken","lastName":"Speaker","email":"badtoken@example.com","profilePicture":"https://example.com/pic.jpg"}'

echo ""
echo ""

echo "=========================================="
echo "  FORBIDDEN (403)"
echo "=========================================="
echo ""

echo "=== Test #36: [403] participant JWT (role=PARTICIPANT, not ADMIN) ==="
curlie -b /tmp/eventsync_participant.txt -X POST :8080/speakers -H 'Content-Type: application/json' -d '{"firstName":"Participant","lastName":"Speaker","email":"participant@example.com","profilePicture":"https://example.com/pic.jpg"}'

rm -f /tmp/eventsync_admin.txt /tmp/eventsync_participant.txt
