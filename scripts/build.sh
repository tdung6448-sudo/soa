#!/usr/bin/env bash
# Biên dịch cả ServerApp và ClientApp sang thư mục build/ của từng project.
# Dùng khi không có NetBeans: cd <repo>; bash scripts/build.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

for app in ServerApp ClientApp; do
  echo "=== Build $app ==="
  cd "$ROOT/$app"
  rm -rf build
  mkdir -p build
  find src -name '*.java' -print0 | xargs -0 javac -source 1.8 -target 1.8 -d build
  echo "OK -> $ROOT/$app/build"
done

echo "Done."
