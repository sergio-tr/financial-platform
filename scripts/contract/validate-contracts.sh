#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

python3 scripts/governance/validate-ser-93-problem-codes.py
python3 scripts/governance/validate-ser-93-domain-event-codes.py
echo "CONTRACT_VALIDATORS_PASS"