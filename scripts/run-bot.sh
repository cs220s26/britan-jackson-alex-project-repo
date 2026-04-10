#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

[[ -f pom.xml ]] || { echo "Run from repo root (need pom.xml here): $ROOT" >&2; exit 1; }

run_root() { [[ $(id -u) -eq 0 ]] && "$@" || sudo "$@"; }

if ! command -v java &>/dev/null || ! command -v mvn &>/dev/null; then
  if command -v dnf &>/dev/null; then run_root dnf install -y java-21-amazon-corretto-headless maven
  elif command -v yum &>/dev/null; then run_root yum install -y java-21-amazon-corretto-headless maven
  elif command -v apt-get &>/dev/null; then
    export DEBIAN_FRONTEND=noninteractive
    run_root apt-get update -qq && run_root apt-get install -y openjdk-21-jdk-headless maven
  else
    echo "Install Java 21 and Maven, then re-run." >&2
    exit 1
  fi
fi

export AWS_REGION="${AWS_REGION:-us-east-1}"
export AWS_SECRET_NAME="${AWS_SECRET_NAME:-220_Discord_Token}"

mvn -q -DskipTests package
exec java -jar "$ROOT/target/discord-bot-1.0.0.jar"
