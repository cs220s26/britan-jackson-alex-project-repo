#!/usr/bin/env bash
set -euo pipefail

# EC2 user-data for "Docker + systemd" deployment.
# NOTE: In some vockey/Learner Lab setups, Amazon Linux 2023 repos do NOT include
# Docker or Podman packages. If you see "No match for argument: podman/docker",
# use an Ubuntu AMI instead.
# - installs Docker Engine + Docker Compose plugin
# - clones the repo into /opt/discord-bot
# - installs and enables bot.service (which runs Compose in the foreground)
#
# The bot reads its Discord token from AWS Secrets Manager.
# Best practice is to attach an IAM instance profile that can read the secret.

REPO_URL="https://github.com/cs220s26/britan-jackson-alex-project-repo.git"
APP_DIR="/opt/discord-bot"

source /etc/os-release

if [[ "${ID:-}" != "ubuntu" ]]; then
  echo "ERROR: This user-data script expects an Ubuntu AMI (ID=ubuntu)."
  echo "You are running: ${PRETTY_NAME:-unknown}"
  echo "In vockey, Ubuntu AMIs reliably support Docker installs via apt."
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y git ca-certificates curl

# Docker Engine + Compose plugin from Ubuntu repos (simple + reliable in labs)
apt-get install -y docker.io docker-compose-plugin
systemctl enable --now docker

if [[ ! -d "$APP_DIR/.git" ]]; then
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

cd "$APP_DIR"

cp "$APP_DIR/bot.service" /etc/systemd/system/bot.service
systemctl daemon-reload
systemctl enable --now bot.service
