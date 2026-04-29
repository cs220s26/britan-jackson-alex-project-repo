#!/usr/bin/env bash
set -euo pipefail

HOST="${REDIS_HOST:-127.0.0.1}"
PORT="${REDIS_PORT:-6379}"
DB="${REDIS_DB:-0}"

if ! command -v redis-cli >/dev/null 2>&1; then
  echo "redis-cli not found. Install Redis (or at least redis-cli) first." >&2
  exit 1
fi

run() {
  redis-cli -h "${HOST}" -p "${PORT}" -n "${DB}" --raw "$@"
}

ts_now() {
  # ISO-8601 UTC, compatible with Jackson Instant parsing
  python3 - <<'PY'
from datetime import datetime, timezone
print(datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
PY
}

if ! run PING >/dev/null 2>&1; then
  echo "Can't connect to Redis at ${HOST}:${PORT} (db=${DB})." >&2
  echo "Start Redis locally, then re-run this script." >&2
  exit 1
fi

echo "Seeding NEW BOT dataset into Redis at ${HOST}:${PORT} db=${DB} ..."

# Keep dataset small: a couple of users, one group, no leaderboard activity yet.
# Matches RedisManager/FakeRedisRepository key prefixes:
# - user:<id>
# - group:<name>
# - session:<sessionId>
# - activeSession:<userId>

U1="u1001"
U2="u1002"
G1="math"

run SET "user:${U1}" "{\"id\":\"${U1}\",\"username\":\"Alex\",\"totalXP\":0,\"groups\":[\"${G1}\"]}" >/dev/null
run SET "user:${U2}" "{\"id\":\"${U2}\",\"username\":\"Britan\",\"totalXP\":0,\"groups\":[\"${G1}\"]}" >/dev/null

# Empty leaderboard, but members exist.
run SET "group:${G1}" "{\"name\":\"${G1}\",\"members\":[\"${U1}\",\"${U2}\"],\"leaderboard\":{}}" >/dev/null

# One active session (no XP yet until ended).
SID="s-new-1"
START="$(ts_now)"
run SET "session:${SID}" "{\"sessionId\":\"${SID}\",\"userId\":\"${U1}\",\"subject\":\"${G1}\",\"startTime\":\"${START}\",\"duration\":0}" >/dev/null
run SET "activeSession:${U1}" "${SID}" >/dev/null

echo "Done."
echo "Created keys:"
echo "  user:${U1}"
echo "  user:${U2}"
echo "  group:${G1}"
echo "  session:${SID}"
echo "  activeSession:${U1}"
