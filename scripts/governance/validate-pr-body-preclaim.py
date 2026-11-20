#!/usr/bin/env python3
"""Validate a draft PR body before opening a pull request (SER-96 / R9-02)."""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.branch_commit import parse_branch_ser, validate_branch  # noqa: E402
from traceability.pr_parser import validate_pr_body  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(
        description="SER-96 pre-claim PR body validator (envelope + sections + Linear ids)",
    )
    parser.add_argument("--branch", required=True, help="Target branch name (e.g. cursor/ser-96-…)")
    parser.add_argument("--body-file", type=Path, required=True, help="Draft PR description markdown")
    args = parser.parse_args()

    errors = list(validate_branch(args.branch))
    branch_ser = parse_branch_ser(args.branch)
    body = args.body_file.read_text(encoding="utf-8")
    errors.extend(validate_pr_body(body, branch_ser=branch_ser))

    if errors:
        for err in errors:
            print(err.format(), file=sys.stderr)
        return 2

    print(f"PR_BODY_PRECLAIM_PASS primary={branch_ser}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())