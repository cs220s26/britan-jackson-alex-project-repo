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

# Amazon Linux 2023 uses dnf (yum is a compatibility wrapper).
dnf -y update
dnf -y install git curl podman python3-pip

# Podman Compose (pip) so we can run docker-compose.yml under systemd
python3 -m pip install --upgrade pip
python3 -m pip install podman-compose

# Make sure the systemd unit can find it at a stable path
ln -sf "$(command -v podman-compose)" /usr/local/bin/podman-compose

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

cd "$APP_DIR"

cp "$APP_DIR/bot.service" /etc/systemd/system/bot.service
systemctl daemon-reload
systemctl enable --now bot.service
