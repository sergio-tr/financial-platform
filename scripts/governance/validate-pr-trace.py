#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.branch_commit import (  # noqa: E402
    is_legacy_pr1_exception,
    parse_branch_ser,
    validate_branch,
    validate_contribution_metadata,
    validate_conventional_subject,
)
from traceability.pr_parser import validate_pr_body  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="SER-96 PR/branch/commit policy validator")
    parser.add_argument("--branch", required=True)
    parser.add_argument("--pr-body", type=Path, required=True)
    parser.add_argument("--pr-title", default="")
    parser.add_argument("--pr-number", type=int, default=None)
    parser.add_argument("--commit-subject", default="")
    parser.add_argument("--commit-body", default="")
    args = parser.parse_args()

    if is_legacy_pr1_exception(args.pr_number, args.branch):
        print("PR_TRACE_PASS legacy=LEGACY-PR-001")
        return 0

    errors = []
    errors.extend(validate_branch(args.branch))
    branch_ser = parse_branch_ser(args.branch)
    body = args.pr_body.read_text()
    errors.extend(validate_pr_body(body, branch_ser=branch_ser))
    errors.extend(
        validate_contribution_metadata(
            args.pr_title,
            args.commit_subject,
            args.commit_body,
            check_assistant_names=True,
        )
    )
    errors.extend(validate_contribution_metadata(body, check_assistant_names=False))
    if args.commit_subject:
        errors.extend(validate_conventional_subject(args.commit_subject))

    if errors:
        for err in errors:
            print(err.format(), file=sys.stderr)
        return 2

    print(f"PR_TRACE_PASS primary={branch_ser}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
