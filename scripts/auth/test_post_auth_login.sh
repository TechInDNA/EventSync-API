#!/bin/bash
echo "=== [1] [200] valid admin credentials — expect: token + user (id, firstName, lastName, email, role=ADMIN) ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123

echo ""
echo "=== [2] [200] same request again — expect: new token, same user ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123

echo ""
echo ""
echo "=== VALIDATION ERRORS (400 / 422) ==="
echo ""

echo "=== [3] [400] empty body — HttpMessageNotReadableException → status=400 ==="
curlie POST :8080/auth/login

echo ""
echo "=== [4] [422] null email — validateEmail(null) throws ==="
curlie POST :8080/auth/login password=admin123

echo ""
echo "=== [5] [422] empty email — validateEmail('') throws ==="
curlie POST :8080/auth/login email='' password=admin123

echo ""
echo "=== [6] [422] blank email (spaces) — validateEmail('   ') throws ==="
curlie POST :8080/auth/login email='   ' password=admin123

echo ""
echo "=== [7] [422] invalid email format (no @) — VALID_EMAIL_PATTERN fails ==="
curlie POST :8080/auth/login email=not-an-email password=admin123

echo ""
echo "=== [8] [422] invalid email format (no domain) — VALID_EMAIL_PATTERN fails ==="
curlie POST :8080/auth/login email='user@' password=admin123

echo ""
echo "=== [9] [422] invalid email char (!) — ALLOWED_EMAIL_CHAR fails ==="
curlie POST :8080/auth/login email='user!@domain.com' password=admin123

echo ""
echo "=== [10] [422] email too long (>50 chars) — lengthValidation fails ==="
curlie POST :8080/auth/login email='aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee@domain.com' password=admin123

echo ""
echo "=== [11] [422] email with + — '+' not in ALLOWED_EMAIL_CHAR ==="
curlie POST :8080/auth/login email='admin+tag@domain.com' password=admin123

echo ""
echo "=== [12] [422] email with accented chars — 'é' not in ALLOWED_EMAIL_CHAR ==="
curlie POST :8080/auth/login email='admin@domaîné.com' password=admin123

echo ""
echo "=== [13] [422] null password — checkNullData('password', null) throws ==="
curlie POST :8080/auth/login email=admin@eventsync.com

echo ""
echo "=== [14] [422] empty password — checkNullData('password', '') throws ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=''

echo ""
echo "=== [15] [422] blank password (spaces) — checkNullData('password', '   ') throws ==="
curlie POST :8080/auth/login email=admin@eventsync.com password='   '

echo ""
echo ""
echo "=== AUTH FAILURES (401) — single cases, counter=0 after reset ==="
echo ""

echo "=== [16] [200] reset counter before 401 tests ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123

echo ""
echo "=== [17] [401] unknown email — findByEmail → empty → admin=null → increment → 401 ==="
curlie POST :8080/auth/login email=unknown@example.com password=admin123

echo ""
echo "=== [18] [401] wrong password — passwordEncoder.matches → false → increment → 401 ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=wrongpassword

echo ""
echo "=== [19] [401] non-admin role — findByEmail → filtered out (≠ADMIN) → null → increment → 401 ==="
curlie POST :8080/auth/login email=speaker@eventsync.com password=somepassword

echo ""
echo ""
echo "=== MALFORMED REQUESTS (400) ==="
echo ""

echo "=== [20] [200] reset counter before malformed / rate-limit section ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123

echo ""
echo "=== [21] [400] malformed JSON body — Jackson parse error → 400 ==="
curlie POST :8080/auth/login -H 'Content-Type: application/json' -d '{bad}'

echo ""
echo "=== [22] [415] wrong content type (text/plain) — HttpMediaTypeNotSupportedException → 415 ==="
curlie POST :8080/auth/login -H 'Content-Type: text/plain' -d 'not json'

echo ""
echo ""
echo "=== RATE LIMITING (429) — after this block the IP is blacklisted, no further requests pass ==="
echo ""

echo "=== [23] [200] reset counter ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123

echo ""
echo "=== [24] [401] fail 1/5 — expect: '4 attempt(s) left' ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=fail1

echo ""
echo "=== [25] [401] fail 2/5 — expect: '3 attempt(s) left' ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=fail2

echo ""
echo "=== [26] [401] fail 3/5 — expect: '2 attempt(s) left' ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=fail3

echo ""
echo "=== [27] [401] fail 4/5 — expect: '1 attempt(s) left' ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=fail4

echo ""
echo "=== [28] [429] fail 5/5 — blocked due to too many failed login attempts ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=fail5

echo ""
echo "=== [29] [401] blocked IP even with correct credentials — checkBlacklist → 'malicious behavior' ==="
curlie POST :8080/auth/login email=admin@eventsync.com password=admin123
