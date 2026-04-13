#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

[[ -f pom.xml ]] || { echo "Run from repo root (need pom.xml here): $ROOT" >&2; exit 1; }

run_root() { [[ $(id -u) -eq 0 ]] && "$@" || sudo "$@"; }

# POM uses --release 17; Maven must use JDK 17+ (not an older default java on the AMI).
ensure_jdk_17() {
  local c
  for c in \
    /usr/lib/jvm/java-17-amazon-corretto \
    /usr/lib/jvm/java-17-amazon-corretto.x86_64 \
    /usr/lib/jvm/java-17-amazon-corretto.aarch64 \
    /usr/lib/jvm/java-17-openjdk \
    /usr/lib/jvm/java-17 \
    /usr/lib/jvm/java-21-amazon-corretto \
    /usr/lib/jvm/java-21-amazon-corretto.x86_64 \
    /usr/lib/jvm/java-21-amazon-corretto.aarch64; do
    if [[ -x "$c/bin/javac" ]] && "$c/bin/javac" -version 2>&1 | grep -qE ' 1[7-9]\.| 2[0-9]\.'; then
      export JAVA_HOME="$c"
      export PATH="$JAVA_HOME/bin:$PATH"
      return 0
    fi
  done
  return 1
}

if ! command -v mvn &>/dev/null || ! ensure_jdk_17; then
  if command -v dnf &>/dev/null; then
    run_root dnf install -y java-17-amazon-corretto-devel maven
  elif command -v yum &>/dev/null; then
    run_root yum install -y java-17-amazon-corretto-devel maven
  elif command -v apt-get &>/dev/null; then
    export DEBIAN_FRONTEND=noninteractive
    run_root apt-get update -qq && run_root apt-get install -y openjdk-17-jdk-headless maven
  else
    echo "Install Java 17+ JDK and Maven, then re-run." >&2
    exit 1
  fi
fi

ensure_jdk_17 || {
  echo "No JDK 17+ found under /usr/lib/jvm. Install java-17-amazon-corretto-devel (or set JAVA_HOME)." >&2
  exit 1
}

export AWS_REGION="${AWS_REGION:-us-east-1}"
export AWS_SECRET_NAME="${AWS_SECRET_NAME:-220_Discord_Token}"

mvn -q package
exec java -jar "$ROOT/target/discord-bot-1.0.0.jar"
