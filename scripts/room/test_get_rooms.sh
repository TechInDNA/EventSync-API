#!/bin/bash

echo ""
echo "=== SUCCESS === (15 rooms seeded: Main Hall, Meeting Room A/B, Salle x4, Auditorium, Atelier x2, Workshop Area, Espace Détente, Salon VIP, Backstage Room, Rooftop Terrace)"
echo ""

echo "=== [1]  [200] default pagination (page=1, size=10) — expect: 10 rooms ==="
curlie GET :8080/rooms

echo ""
echo "=== [2]  [200] page=2, size=10 — expect: 5 rooms ==="
curlie GET ':8080/rooms?page=2&size=10'

echo ""
echo "=== [3]  [200] page=1, size=5 — expect: 5 rooms ==="
curlie GET ':8080/rooms?page=1&size=5'

echo ""
echo "=== [4]  [200] size=50 (all rooms) — expect: 15 rooms ==="
curlie GET ':8080/rooms?page=1&size=50'

echo ""
echo "=== [5]  [200] page out of range (page=999) — expect: 0 rooms ==="
curlie GET ':8080/rooms?page=999&size=10'

echo ""
echo "=== [6]  [200] name=Room — expect: 3 rooms (Meeting Room A/B, Backstage Room) ==="
curlie GET :8080/rooms?name=Room

echo ""
echo "=== [7]  [200] name=room — case-insensitive, expect: 3 rooms ==="
curlie GET :8080/rooms?name=room

echo ""
echo "=== [8]  [200] name=Salle — expect: 4 rooms ==="
curlie GET :8080/rooms?name=Salle

echo ""
echo "=== [9]  [200] name=Atelier — expect: 2 rooms (Python, DevOps) ==="
curlie GET :8080/rooms?name=Atelier

echo ""
echo "=== [10] [200] name=Main — expect: 1 room (Main Hall) ==="
curlie GET :8080/rooms?name=Main

echo ""
echo "=== [11] [200] name=Main Hall — exact match, expect: 1 room ==="
curlie GET :8080/rooms?name=Main%20Hall

echo ""
echo "=== [12] [200] name=a — partial, expect: multi rooms (case-insensitive) ==="
curlie GET :8080/rooms?name=a

echo ""
echo "=== [13] [200] name=Nonexistent — expect: 0 rooms ==="
curlie GET :8080/rooms?name=Nonexistent

echo ""
echo "=== [14] [200] name=a & page=1, size=2 — paginated search, expect: 2 rooms ==="
curlie GET ':8080/rooms?name=a&page=1&size=2'

echo ""
echo "=== [15] [200] name=a & page=2, size=2 — paginated search page 2, expect: 2 rooms ==="
curlie GET ':8080/rooms?name=a&page=2&size=2'

echo ""
echo "=== [16] [200] name= (empty string) — treated as no filter, expect: 10 rooms ==="
curlie GET ':8080/rooms?name='

echo ""
echo ""
echo "=== EXCEPTION TESTS ==="
echo ""

echo "=== [17] [400] page=abc — invalid int for page ==="
curlie GET :8080/rooms?page=abc

echo ""
echo "=== [18] [400] size=xyz — invalid int for size ==="
curlie GET :8080/rooms?size=xyz

echo ""
echo "=== [19] [422] name=Hello! — invalid char ! rejected by validateSearchString ==="
curlie GET :8080/rooms?name=Hello!

echo ""
echo "=== [20] [422] name=foo@bar — invalid char @ rejected by validateSearchString ==="
curlie GET :8080/rooms?name=foo@bar

echo ""
echo "=== [21] [422] name=spe&cial — invalid char & (URL-encoded) rejected by validateSearchString ==="
curlie GET :8080/rooms?name=spe%26cial

echo ""
echo ""
echo "=== EDGE CASES (graceful handling) ==="
echo ""

echo "=== [22] [200] page=0 — normalised to page=1, expect: 10 rooms ==="
curlie GET :8080/rooms?page=0

echo ""
echo "=== [23] [200] page=-5 — normalised to page=1, expect: 10 rooms ==="
curlie GET :8080/rooms?page=-5

echo ""
echo "=== [24] [200] size=0 — normalised to size=10, expect: 10 rooms ==="
curlie GET :8080/rooms?size=0

echo ""
echo "=== [25] [200] size=-1 — normalised to size=10, expect: 10 rooms ==="
curlie GET :8080/rooms?size=-1

echo ""
echo "=== [26] [200] page=1&size=0 — both normalised, expect: 10 rooms ==="
curlie GET ':8080/rooms?page=1&size=0'

echo ""
echo "=== [27] [200] name=a'z — apostrophe allowed by validateSearchString, expect: 0 rooms ==="
curlie GET :8080/rooms?name=a%27z

echo ""
echo "=== [28] [200] name=123 — digits allowed by validateSearchString, expect: 0 rooms ==="
curlie GET :8080/rooms?name=123

echo ""
echo "=== [29] [200] name= (no value via query string) — treated as no filter, expect: 10 rooms ==="
curlie GET :8080/rooms?name=
