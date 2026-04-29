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

ts_ago_minutes() {
  local minutes="${1}"
  python3 - <<PY
from datetime import datetime, timezone, timedelta
minutes = int("${minutes}")
dt = datetime.now(timezone.utc) - timedelta(minutes=minutes)
print(dt.strftime("%Y-%m-%dT%H:%M:%SZ"))
PY
}

if ! run PING >/dev/null 2>&1; then
  echo "Can't connect to Redis at ${HOST}:${PORT} (db=${DB})." >&2
  echo "Start Redis locally, then re-run this script." >&2
  exit 1
fi

echo "Seeding MID-LIFE BOT dataset into Redis at ${HOST}:${PORT} db=${DB} ..."

# A richer dataset: multiple groups, users with XP, and leaderboards with history.
U1="u2001" # Alex
U2="u2002" # Britan
U3="u2003" # Chris
U4="u2004" # Dana

G1="math"
G2="coding"

# Users
run SET "user:${U1}" "{\"id\":\"${U1}\",\"username\":\"Alex\",\"totalXP\":120,\"groups\":[\"${G1}\",\"${G2}\"]}" >/dev/null
run SET "user:${U2}" "{\"id\":\"${U2}\",\"username\":\"Britan\",\"totalXP\":55,\"groups\":[\"${G1}\"]}" >/dev/null
run SET "user:${U3}" "{\"id\":\"${U3}\",\"username\":\"Chris\",\"totalXP\":210,\"groups\":[\"${G2}\"]}" >/dev/null
run SET "user:${U4}" "{\"id\":\"${U4}\",\"username\":\"Dana\",\"totalXP\":15,\"groups\":[\"${G1}\",\"${G2}\"]}" >/dev/null

# Groups with leaderboards
run SET "group:${G1}" \
  "{\"name\":\"${G1}\",\"members\":[\"${U1}\",\"${U2}\",\"${U4}\"],\"leaderboard\":{\"${U1}\":70,\"${U2}\":40,\"${U4}\":15}}" >/dev/null
run SET "group:${G2}" \
  "{\"name\":\"${G2}\",\"members\":[\"${U1}\",\"${U3}\",\"${U4}\"],\"leaderboard\":{\"${U3}\":160,\"${U1}\":50,\"${U4}\":15}}" >/dev/null

# A few historical sessions
S1="s-mid-1"
S2="s-mid-2"
S3="s-mid-3"

run SET "session:${S1}" "{\"sessionId\":\"${S1}\",\"userId\":\"${U1}\",\"subject\":\"${G1}\",\"startTime\":\"$(ts_ago_minutes 180)\",\"duration\":30}" >/dev/null
run SET "session:${S2}" "{\"sessionId\":\"${S2}\",\"userId\":\"${U3}\",\"subject\":\"${G2}\",\"startTime\":\"$(ts_ago_minutes 90)\",\"duration\":45}" >/dev/null
run SET "session:${S3}" "{\"sessionId\":\"${S3}\",\"userId\":\"${U2}\",\"subject\":\"${G1}\",\"startTime\":\"$(ts_ago_minutes 60)\",\"duration\":20}" >/dev/null

# One currently active session (so !session end awards more XP and bumps leaderboards)
ACTIVE_SID="s-mid-active"
run SET "session:${ACTIVE_SID}" \
  "{\"sessionId\":\"${ACTIVE_SID}\",\"userId\":\"${U4}\",\"subject\":\"${G2}\",\"startTime\":\"$(ts_ago_minutes 12)\",\"duration\":0}" >/dev/null
run SET "activeSession:${U4}" "${ACTIVE_SID}" >/dev/null

echo "Done."
echo "Tip: run \`!leaderboard ${G1}\` or \`!leaderboard ${G2}\` after starting the bot."
echo "Active session user: ${U4} (use !session end to award XP)"
