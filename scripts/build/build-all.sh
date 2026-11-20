#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

./mvnw -B -q clean package -DskipTests
if command -v pnpm >/dev/null; then
  pnpm install --frozen-lockfile 2>/dev/null || pnpm install
  pnpm run build
else
  echo "SKIP frontend build: pnpm not available"
fi