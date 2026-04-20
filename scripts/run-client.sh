#!/usr/bin/env bash
# Chạy Client sau khi đã chạy scripts/build.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
java -cp "$ROOT/ClientApp/build" clientapp.ClientMain "$@"
