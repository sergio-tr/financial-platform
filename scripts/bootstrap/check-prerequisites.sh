#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

echo "== Java (expect 25) =="
if command -v java >/dev/null; then java -version; else echo "NOT_FOUND"; fi

echo "== Node (expect 24.x) =="
if command -v node >/dev/null; then node -v; else echo "NOT_FOUND"; fi

echo "== pnpm (expect 12.7.0) =="
if command -v corepack >/dev/null; then
  corepack enable >/dev/null 2>&1 || true
fi
if command -v pnpm >/dev/null; then pnpm -v; else echo "NOT_FOUND"; fi

echo "== Maven wrapper =="
test -x ./mvnw && ./mvnw -v || echo "mvnw NOT_RUN"