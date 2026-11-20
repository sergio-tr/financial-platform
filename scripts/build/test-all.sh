#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

./mvnw -B verify
if command -v pnpm >/dev/null; then
  pnpm install --frozen-lockfile 2>/dev/null || pnpm install
  pnpm run test
else
  echo "SKIP frontend tests: pnpm not available"
fi