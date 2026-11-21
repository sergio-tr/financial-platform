#!/usr/bin/env python3
"""SER-160 / SER-109 slice: validate Identity field-level privacy catalog YAML."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from field_privacy_catalog_lib import ROOT, SCHEMA_PATH, validate_catalog_file

DEFAULT_CATALOG = ROOT / "docs/security/field-privacy-catalog/identity-workspace-session-mfa.v1.yaml"
PASS_TAG = "SER160"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "catalog",
        nargs="?",
        type=Path,
        default=DEFAULT_CATALOG,
        help="Field privacy catalog YAML",
    )
    parser.add_argument(
        "--expect-fail",
        action="store_true",
        help="Expect validation failure (negative fixture harness)",
    )
    args = parser.parse_args()

    if not SCHEMA_PATH.is_file():
        print(f"{PASS_TAG}_CATALOG_FAIL missing_schema={SCHEMA_PATH}", file=sys.stderr)
        return 2
    if not args.catalog.is_file():
        print(f"{PASS_TAG}_CATALOG_FAIL missing_catalog={args.catalog}", file=sys.stderr)
        return 2

    ok, errors = validate_catalog_file(args.catalog)

    if args.expect_fail:
        if ok:
            print(f"{PASS_TAG}_CATALOG_FAIL expected_failure_but_passed", file=sys.stderr)
            return 2
        print(f"{PASS_TAG}_CATALOG_NEGATIVE_PASS")
        return 0

    if not ok:
        for line in errors:
            print(line, file=sys.stderr)
        print(f"{PASS_TAG}_CATALOG_FAIL count={len(errors)}", file=sys.stderr)
        return 2

    print(f"{PASS_TAG}_CATALOG_PASS catalog={args.catalog.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())