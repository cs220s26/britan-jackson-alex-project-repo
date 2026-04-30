#!/usr/bin/env bash
set -euo pipefail

# EC2 user-data for "Docker + systemd" deployment (Amazon Linux 2023).
# - installs Docker + Compose plugin
# - clones the repo into /opt/discord-bot
# - installs and enables bot.service (which runs Compose in the foreground)
#
# The bot reads its Discord token from AWS Secrets Manager.
# Best practice is to attach an IAM instance profile that can read the secret.

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"

yum -y update
yum -y install git docker curl

systemctl enable --now docker

# Install Docker Compose (as a Docker CLI plugin) in a way that works on AL2023
# even when the docker-compose-plugin RPM isn't available.
mkdir -p /usr/local/lib/docker/cli-plugins
if ! /usr/bin/docker compose version >/dev/null 2>&1; then
  curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
  chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
fi

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

cd "$APP_DIR"

cp "$APP_DIR/bot.service" /etc/systemd/system/bot.service
systemctl daemon-reload
systemctl enable --now bot.service
