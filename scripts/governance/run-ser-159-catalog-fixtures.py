#!/usr/bin/env python3
"""Run SER-159 negative catalog fixtures; must fail semantic/schema validation."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FIX = ROOT / "test/fixtures/governance/ser-159"
VALIDATOR = ROOT / "scripts/governance/validate-ser-159-physical-catalog.py"


def main() -> int:
    if not FIX.is_dir():
        print(f"SER159_FIXTURES_FAIL missing_dir={FIX}", file=sys.stderr)
        return 2

    negatives = sorted(FIX.glob("negative/*.yaml"))
    if not negatives:
        print("SER159_FIXTURES_FAIL no negative fixtures", file=sys.stderr)
        return 2

    for path in negatives:
        proc = subprocess.run(
            [sys.executable, str(VALIDATOR), str(path), "--expect-fail"],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        if proc.returncode != 0:
            print(f"SER159_FIXTURES_FAIL fixture={path.name}", file=sys.stderr)
            print(proc.stdout, file=sys.stderr)
            print(proc.stderr, file=sys.stderr)
            return 2

    print(f"SER159_FIXTURES_PASS negatives={len(negatives)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())