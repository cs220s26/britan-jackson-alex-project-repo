#!/usr/bin/env bash
set -euo pipefail

# EC2 user-data for "Docker + systemd" deployment (Ubuntu AMI).
# - installs Docker Engine
# - installs Docker Compose (docker CLI plugin) if missing
# - clones/pulls repo into /opt/discord-bot
# - installs + enables bot.service (runs docker compose in foreground)

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"
SERVICE_PATH="/etc/systemd/system/bot.service"
COMPOSE_PLUGIN_DIR="/usr/local/lib/docker/cli-plugins"

log() { echo "[userdata] $*"; }

source /etc/os-release || true
if [[ "${ID:-}" != "ubuntu" ]]; then
  log "ERROR: expected Ubuntu AMI (ID=ubuntu), got: ${PRETTY_NAME:-unknown}"
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
log "Installing prerequisites"
apt-get update -y
apt-get install -y git ca-certificates curl

log "Installing Docker Engine"
apt-get install -y docker.io
systemctl enable --now docker

log "Ensuring Docker Compose is available"
mkdir -p "$COMPOSE_PLUGIN_DIR"
if ! docker compose version >/dev/null 2>&1; then
  arch="$(uname -m)"
  curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${arch}" -o "$COMPOSE_PLUGIN_DIR/docker-compose"
  chmod +x "$COMPOSE_PLUGIN_DIR/docker-compose"
fi

log "Cloning/updating repo in $APP_DIR"
if [[ -d "$APP_DIR/.git" ]]; then
  git -C "$APP_DIR" fetch --prune origin
  git -C "$APP_DIR" checkout main
  git -C "$APP_DIR" pull --ff-only origin main
else
  rm -rf "$APP_DIR"
  git clone --branch main "$REPO_URL" "$APP_DIR"
fi

cd "$APP_DIR"

log "Installing systemd unit"
cp "$APP_DIR/bot.service" "$SERVICE_PATH"
systemctl daemon-reload
systemctl enable --now bot.service

log "Done. Service status:"
systemctl --no-pager --full status bot.service || true
