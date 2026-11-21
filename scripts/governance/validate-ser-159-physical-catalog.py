#!/usr/bin/env python3
"""SER-159 / SER-103 slice: validate PostgreSQL physical security catalog YAML."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
SCHEMA_PATH = ROOT / "docs/security/postgres-physical-catalog.schema.json"
DEFAULT_CATALOG = ROOT / "docs/security/postgres-physical-catalog/identity-workspace-session.v1.yaml"

RUNTIME_ROLES = frozenset(
    {
        "sergiotr_gateway",
        "sergiotr_worker",
        "sergiotr_migrator",
        "sergiotr_backup",
        "sergiotr_db_owner",
    }
)
FORBIDDEN_RUNTIME_PRIVS = frozenset({"TRUNCATE", "BYPASSRLS"})


def load_yaml(path: Path) -> dict:
    return yaml.safe_load(path.read_text())


def validate_schema(catalog: dict, schema: dict) -> list[str]:
    errors: list[str] = []
    validator = Draft202012Validator(schema)
    for err in sorted(validator.iter_errors(catalog), key=lambda e: list(e.path)):
        loc = ".".join(str(p) for p in err.path) or "(root)"
        errors.append(f"SCHEMA {loc}: {err.message}")
    return errors


def validate_semantics(catalog: dict) -> list[str]:
    errors: list[str] = []
    declared_roles = set(catalog.get("runtimeDatabaseRoles") or [])
    if declared_roles != RUNTIME_ROLES:
        errors.append(
            f"SER159_ROLE_SET_MISMATCH expected={sorted(RUNTIME_ROLES)} actual={sorted(declared_roles)}"
        )

    keys: set[str] = set()
    for idx, obj in enumerate(catalog.get("objects") or []):
        prefix = f"objects[{idx}]"
        key = obj.get("objectKey")
        if key in keys:
            errors.append(f"{prefix} duplicate objectKey={key}")
        keys.add(key)

        status = obj.get("materializationStatus")
        sec = obj.get("securityClass")
        if sec == "UNCLASSIFIED":
            errors.append(f"{prefix} forbidden securityClass UNCLASSIFIED")

        if status == "BLOCKED_DECISION":
            if not obj.get("blockedGapId"):
                errors.append(f"{prefix} BLOCKED_DECISION requires blockedGapId")
            if not obj.get("blockedReason"):
                errors.append(f"{prefix} BLOCKED_DECISION requires blockedReason")
            if obj.get("schema") or obj.get("objectName"):
                errors.append(
                    f"{prefix} BLOCKED_DECISION must not invent schema/objectName (gap ratification required)"
                )
            continue

        if status != "NORMATIVE_PLANNED":
            errors.append(f"{prefix} unknown materializationStatus={status}")
            continue

        for req in (
            "schema",
            "objectName",
            "objectKind",
            "rls",
            "rolePrivileges",
            "migrationOwnerRole",
            "searchPathQualification",
        ):
            if not obj.get(req):
                errors.append(f"{prefix} NORMATIVE_PLANNED missing {req}")

        if sec == "WORKSPACE_SENSITIVE":
            own = obj.get("ownershipColumns") or {}
            if not own.get("workspaceId"):
                errors.append(f"{prefix} WORKSPACE_SENSITIVE requires ownershipColumns.workspaceId")
            rls = obj.get("rls") or {}
            if not rls.get("applicable"):
                errors.append(f"{prefix} WORKSPACE_SENSITIVE requires rls.applicable=true")
            if not rls.get("enabled") or not rls.get("force"):
                errors.append(f"{prefix} WORKSPACE_SENSITIVE requires rls.enabled and rls.force true")
            if not rls.get("usingIntent") or not rls.get("withCheckIntent"):
                errors.append(f"{prefix} WORKSPACE_SENSITIVE requires USING/WITH CHECK intent")

        if sec == "SYSTEM_SECURITY":
            for row in obj.get("rolePrivileges") or []:
                if row.get("role") == "sergiotr_worker" and "SELECT" in (row.get("operations") or []):
                    errors.append(
                        f"{prefix} SYSTEM_SECURITY must not grant sergiotr_worker SELECT"
                    )

        for row in obj.get("rolePrivileges") or []:
            role = row.get("role")
            if role not in RUNTIME_ROLES:
                errors.append(f"{prefix} unknown privilege role={role}")
            for op in row.get("operations") or []:
                if op in FORBIDDEN_RUNTIME_PRIVS:
                    errors.append(f"{prefix} forbidden operation {op} on role {role}")
            if role == "sergiotr_gateway" and "TRUNCATE" in (row.get("operations") or []):
                errors.append(f"{prefix} sergiotr_gateway must not hold TRUNCATE")

    return errors


def validate_catalog(path: Path, schema: dict) -> tuple[bool, list[str]]:
    catalog = load_yaml(path)
    errors = validate_schema(catalog, schema)
    if not errors:
        errors.extend(validate_semantics(catalog))
    return (not errors, errors)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "catalog",
        nargs="?",
        type=Path,
        default=DEFAULT_CATALOG,
        help="Catalog YAML (default: identity-workspace-session slice)",
    )
    parser.add_argument(
        "--expect-fail",
        action="store_true",
        help="Expect validation failure (negative fixture harness)",
    )
    args = parser.parse_args()

    if not SCHEMA_PATH.is_file():
        print(f"SER159_CATALOG_FAIL missing_schema={SCHEMA_PATH}", file=sys.stderr)
        return 2
    if not args.catalog.is_file():
        print(f"SER159_CATALOG_FAIL missing_catalog={args.catalog}", file=sys.stderr)
        return 2

    schema = json.loads(SCHEMA_PATH.read_text())
    ok, errors = validate_catalog(args.catalog, schema)

    if args.expect_fail:
        if ok:
            print("SER159_CATALOG_FAIL expected_failure_but_passed", file=sys.stderr)
            return 2
        print("SER159_CATALOG_NEGATIVE_PASS")
        return 0

    if not ok:
        for line in errors:
            print(line, file=sys.stderr)
        print(f"SER159_CATALOG_FAIL count={len(errors)}", file=sys.stderr)
        return 2

    print(f"SER159_CATALOG_PASS catalog={args.catalog.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())