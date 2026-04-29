#!/usr/bin/env bash
set -euo pipefail

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"

yum install -y git java-17-amazon-corretto-devel maven

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

cd "$APP_DIR"
/usr/bin/mvn -q package

cp "$APP_DIR/bot.service" /etc/systemd/system/bot.service
systemctl daemon-reload
systemctl enable --now bot.service
