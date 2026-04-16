#!/usr/bin/env bash
set -euo pipefail

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"

yum install -y git

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

cp "$APP_DIR/scripts/bot.service" /etc/systemd/system/bot.service
systemctl daemon-reload
systemctl enable --now bot.service
