#!/bin/bash

# ──────────────────────────────────────────────
#  GET /sessions — Test script
# ──────────────────────────────────────────────
# Requires seeded data: at least one room, one event, and a few sessions.
# Run after scripts/sessions/test_post_sessions.sh and the event/room/speaker seed scripts.

echo "=== [200] default pagination (page=1, size=20) ==="
curlie GET :8080/sessions

echo ""
echo "=== [200] custom page & size (page=1, size=5) ==="
curlie GET ':8080/sessions?page=1&size=5'

echo ""
echo "=== [200] size=100 (all sessions) ==="
curlie GET ':8080/sessions?page=1&size=100'

echo ""
echo "=== [200] page out of range — expect: 0 sessions ==="
curlie GET ':8080/sessions?page=999&size=20'

echo ""
echo "=== [200] room=Salle — expect: every session in 'Salle Principale' ==="
curlie GET ':8080/sessions?room=Salle'

echo ""
echo "=== [200] room=Nonexistent — expect: 0 sessions ==="
curlie GET ':8080/sessions?room=Nonexistent'

echo ""
echo "=== [200] event=Session Test Event — expect: only sessions tied to that event ==="
curlie GET ':8080/sessions?event=Session%20Test%20Event'

echo ""
echo "=== [200] event=Spring — expect: any session linked to an event whose title contains Spring ==="
curlie GET ':8080/sessions?event=Spring'

echo ""
echo "=== [200] event=Nonexistent — expect: 0 sessions ==="
curlie GET ':8080/sessions?event=Nonexistent'

echo ""
echo "=== [200] speaker=Jane — expect: any session whose speaker first or last name contains 'Jane' ==="
curlie GET ':8080/sessions?speaker=Jane'

echo ""
echo "=== [200] speaker=Nonexistent — expect: 0 sessions ==="
curlie GET ':8080/sessions?speaker=Nonexistent'

echo ""
echo "=== [200] live=true — expect: only currently-live sessions ==="
curlie GET ':8080/sessions?live=true'

echo ""
echo "=== [200] live=false — expect: every non-live session ==="
curlie GET ':8080/sessions?live=false'

echo ""
echo "=== [200] combined filters — room + live=false ==="
curlie GET ':8080/sessions?room=Salle&live=false'

echo ""
echo "=== [200] combined filters — event + live=false ==="
curlie GET ':8080/sessions?event=Session%20Test%20Event&live=false'

echo ""
echo "=== [200] combined filters — room + speaker + live ==="
curlie GET ':8080/sessions?room=Salle&speaker=Jane&live=false'

echo ""
echo "=== [200] page=2 size=10 ==="
curlie GET ':8080/sessions?page=2&size=10'

echo ""
echo ""
echo "=== EXCEPTION TESTS ==="

echo ""
echo "=== [400] page=abc — invalid int for page ==="
curlie GET ':8080/sessions?page=abc'

echo ""
echo "=== [400] size=abc — invalid int for size ==="
curlie GET ':8080/sessions?size=abc'

echo ""
echo "=== [400] live=maybe — invalid Boolean ==="
curlie GET ':8080/sessions?live=maybe'

echo ""
echo "=== [422] room=Salle! — invalid char ! rejected by validateSearchString ==="
curlie GET ':8080/sessions?room=Salle%21'

echo ""
echo "=== [422] event=Event<script> — invalid char < rejected by validateSearchString ==="
curlie GET ':8080/sessions?event=Event%3Cscript%3E'

echo ""
echo "=== [422] speaker=Doe@x — invalid char @ rejected by validateSearchString ==="
curlie GET ':8080/sessions?speaker=Doe%40x'

echo ""
echo ""
echo "=== EDGE CASES (graceful handling) ==="

echo ""
echo "=== [200] room= (empty) — treated as no filter ==="
curlie GET ':8080/sessions?room='

echo ""
echo "=== [200] event= (empty) — treated as no filter ==="
curlie GET ':8080/sessions?event='

echo ""
echo "=== [200] speaker= (empty) — treated as no filter ==="
curlie GET ':8080/sessions?speaker='

echo ""
echo "=== [200] page=0 — normalised to page=1 ==="
curlie GET ':8080/sessions?page=0'

echo ""
echo "=== [200] page=-5 — normalised to page=1 ==="
curlie GET ':8080/sessions?page=-5'

echo ""
echo "=== [200] size=0 — normalised to size=20 ==="
curlie GET ':8080/sessions?size=0'

echo ""
echo "=== [200] size=-1 — normalised to size=20 ==="
curlie GET ':8080/sessions?size=-1'

echo ""
echo "=== [200] page=1&size=0 — normalised ==="
curlie GET ':8080/sessions?page=1&size=0'

echo ""
echo "=== [200] live=false + page=2 size=5 ==="
curlie GET ':8080/sessions?live=false&page=2&size=5'

echo ""
echo "=== [200] all filters combined (room=Salle&event=Session&speaker=Jane&live=false) ==="
curlie GET ':8080/sessions?room=Salle&event=Session%20Test%20Event&speaker=Jane&live=false'