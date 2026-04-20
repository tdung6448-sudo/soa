#!/usr/bin/env bash
# Chạy Server sau khi đã chạy scripts/build.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
java -cp "$ROOT/ServerApp/build" serverapp.ServerMain "$@"
