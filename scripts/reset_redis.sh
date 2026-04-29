#!/usr/bin/env bash
set -euo pipefail

HOST="${REDIS_HOST:-127.0.0.1}"
PORT="${REDIS_PORT:-6379}"
DB="${REDIS_DB:-0}"

if ! command -v redis-cli >/dev/null 2>&1; then
  echo "redis-cli not found. Install Redis (or at least redis-cli) first." >&2
  exit 1
fi

if ! redis-cli -h "${HOST}" -p "${PORT}" -n "${DB}" PING >/dev/null 2>&1; then
  echo "Can't connect to Redis at ${HOST}:${PORT} (db=${DB})." >&2
  echo "Start Redis locally, then re-run this script." >&2
  exit 1
fi

echo "Resetting Redis at ${HOST}:${PORT} db=${DB} ..."
redis-cli -h "${HOST}" -p "${PORT}" -n "${DB}" FLUSHDB >/dev/null
echo "Done."
