#!/bin/bash
echo "=== [200] default pagination (page=1, size=10) — expect: 10 speakers ==="
curlie GET :8080/speakers

echo ""
echo "=== [200] custom page & size (page=1, size=5) — expect: 5 speakers ==="
curlie GET ':8080/speakers?page=1&size=5'

echo ""
echo "=== [200] page=2, size=5 — expect: 5 speakers (speakers 6-10) ==="
curlie GET ':8080/speakers?page=2&size=5'

echo ""
echo "=== [200] page=3, size=5 — expect: 0 speakers ==="
curlie GET ':8080/speakers?page=3&size=5'

echo ""
echo "=== [200] size=100 (all) — expect: 10 speakers ==="
curlie GET ':8080/speakers?page=1&size=100'

echo ""
echo "=== [200] page out of range — expect: 0 speakers ==="
curlie GET ':8080/speakers?page=999&size=10'

echo ""
echo ""
echo "=== SEARCH TESTS ==="
echo ""

echo "=== [200] search=Ada — expect: 1 speaker (Ada Lovelace) ==="
curlie GET :8080/speakers?search=Ada

echo ""
echo "=== [200] search=Lovelace — expect: 1 speaker ==="
curlie GET :8080/speakers?search=Lovelace

echo ""
echo "=== [200] search=Barbara — expect: 2 speakers (Barbara Liskov, Barbara McClintock) ==="
curlie GET :8080/speakers?search=Barbara

echo ""
echo "=== [200] search=linus — expect: 1 speaker (Linus Torvalds) ==="
curlie GET ':8080/speakers?search=linus'

echo ""
echo "=== [200] search=bar — expect: 2 speakers (Barbara Liskov, Barbara McClintock) ==="
curlie GET :8080/speakers?search=bar

echo ""
echo "=== [200] search=Nonexistent — expect: 0 speakers ==="
curlie GET :8080/speakers?search=Nonexistent

echo ""
echo "=== [200] search= (empty) — treated as no filter, expect: 10 speakers ==="
curlie GET :8080/speakers?search=

echo ""
echo "=== [200] search=a — single char allowed by validateSearchString, expect: partial match ==="
curlie GET :8080/speakers?search=a

echo ""
echo "=== [200] search=ab — short string allowed, expect: partial match ==="
curlie GET :8080/speakers?search=ab

echo ""
echo ""
echo "=== EXCEPTION TESTS ==="
echo ""

echo "=== [400] page=abc — invalid int for page ==="
curlie GET :8080/speakers?page=abc

echo ""
echo "=== [400] size=xyz — invalid int for size ==="
curlie GET :8080/speakers?size=xyz

echo ""
echo "=== [422] search=John! — invalid char ! rejected by validateSearchString ==="
curlie GET :8080/speakers?search=John!

echo ""
echo "=== [422] search=test@test — invalid char @ rejected ==="
curlie GET :8080/speakers?search=test@test

echo ""
echo "=== [422] search=hello_world — invalid char _ rejected ==="
curlie GET :8080/speakers?search=hello_world

echo ""
echo ""
echo "=== EDGE CASES (graceful handling) ==="
echo ""

echo "=== [200] page=0 — normalised to page=1, expect: 10 speakers ==="
curlie GET :8080/speakers?page=0

echo ""
echo "=== [200] page=-5 — normalised to page=1, expect: 10 speakers ==="
curlie GET :8080/speakers?page=-5

echo ""
echo "=== [200] size=0 — normalised to size=10, expect: 10 speakers ==="
curlie GET :8080/speakers?size=0

echo ""
echo "=== [200] size=-1 — normalised to size=10, expect: 10 speakers ==="
curlie GET :8080/speakers?size=-1

echo ""
echo "=== [200] page=1&size=0 — normalised, expect: 10 speakers ==="
curlie GET ':8080/speakers?page=1&size=0'
