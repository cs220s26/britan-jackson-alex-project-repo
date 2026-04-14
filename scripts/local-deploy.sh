#!/usr/bin/env bash
# Build and run locally; token always comes from AWS Secrets Manager (see local.env for AWS_PROFILE/region).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

[[ -f pom.xml ]] || { echo "Run from repo: need $ROOT/pom.xml" >&2; exit 1; }

if [[ -f "$ROOT/local.env" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "$ROOT/local.env"
  set +a
fi

mvn -q package
exec java -jar "$ROOT/target/discord-bot-1.0.0.jar"
