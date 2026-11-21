#!/usr/bin/env python3
"""Regression: validate all accepted field-privacy catalog slices (SER-160 + SER-161)."""

from __future__ import annotations

import sys
from pathlib import Path

from field_privacy_catalog_lib import ROOT, SCHEMA_PATH, validate_catalog_file

CATALOGS = (
    ROOT / "docs/security/field-privacy-catalog/identity-workspace-session-mfa.v1.yaml",
    ROOT / "docs/security/field-privacy-catalog/manual-import-portfolio-snapshot.v1.yaml",
)


def main() -> int:
    if not SCHEMA_PATH.is_file():
        print("FIELD_PRIVACY_REGRESSION_FAIL missing_schema", file=sys.stderr)
        return 2

    for path in CATALOGS:
        if not path.is_file():
            print(f"FIELD_PRIVACY_REGRESSION_FAIL missing_catalog={path}", file=sys.stderr)
            return 2
        ok, errors = validate_catalog_file(path)
        if not ok:
            for line in errors:
                print(f"{path.name}: {line}", file=sys.stderr)
            print(f"FIELD_PRIVACY_REGRESSION_FAIL catalog={path.name}", file=sys.stderr)
            return 2
        print(f"FIELD_PRIVACY_REGRESSION_PASS catalog={path.relative_to(ROOT)}")

    print(f"FIELD_PRIVACY_REGRESSION_PASS slices={len(CATALOGS)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())