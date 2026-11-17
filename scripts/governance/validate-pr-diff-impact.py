#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.diff_classifier import classify_paths  # noqa: E402
from traceability.diff_impact import validate_phase0_diff  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="SER-97 Stage B — PR diff path classification (IMPLEMENTATION_LOCK)")
    parser.add_argument(
        "--changed-files",
        type=Path,
        required=True,
        help="Newline-separated repository-relative paths changed in the PR",
    )
    args = parser.parse_args()

    text = args.changed_files.read_text()
    paths = [ln.strip() for ln in text.splitlines() if ln.strip()]
    classes = sorted(classify_paths(paths))
    errors = validate_phase0_diff(paths)

    if errors:
        for err in errors:
            print(err.format(), file=sys.stderr)
        return 2

    print(f"PR_DIFF_IMPACT_PASS classes={','.join(classes) if classes else 'NONE'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())