#!/usr/bin/env bash
set -euo pipefail

# EC2 user-data for "Docker + systemd" deployment (Ubuntu AMI).

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"
SERVICE_PATH="/etc/systemd/system/bot.service"
COMPOSE_PLUGIN_DIR="/usr/local/lib/docker/cli-plugins"

export DEBIAN_FRONTEND=noninteractive

apt-get update -y
apt-get install -y git ca-certificates curl redis-tools
apt-get install -y docker.io
systemctl enable --now docker

mkdir -p "$COMPOSE_PLUGIN_DIR"
arch="$(uname -m)"
curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${arch}" -o "$COMPOSE_PLUGIN_DIR/docker-compose"
chmod +x "$COMPOSE_PLUGIN_DIR/docker-compose"

rm -rf "$APP_DIR"
git clone --branch main "$REPO_URL" "$APP_DIR"

cd "$APP_DIR"

cp "$APP_DIR/bot.service" "$SERVICE_PATH"
systemctl daemon-reload
systemctl enable --now bot.service

systemctl --no-pager --full status bot.service || true
