#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/discord-bot"
SERVICE_NAME="bot.service"
BRANCH="${1:-main}"

if [[ $EUID -ne 0 ]]; then
  exec sudo -E bash "$0" "$BRANCH"
fi

if [[ ! -d "$APP_DIR/.git" ]]; then
  echo "ERROR: $APP_DIR is missing or not a git repo."
  echo "Expected the app to be cloned at $APP_DIR."
  exit 1
fi

cd "$APP_DIR"

echo "Updating repo in $APP_DIR (branch: $BRANCH)"
git fetch --prune origin
git checkout "$BRANCH"
git pull --ff-only origin "$BRANCH"

echo "Restarting $SERVICE_NAME"
systemctl restart "$SERVICE_NAME"

echo "Status:"
systemctl --no-pager --full status "$SERVICE_NAME" || true
