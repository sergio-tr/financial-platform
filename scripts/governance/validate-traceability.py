#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.graph_rules import foundation_invariants, validate_graph  # noqa: E402
from traceability.profile import ValidationProfile, parse_profile  # noqa: E402


def load_yaml(path: Path):
    return yaml.safe_load(path.read_text())


def validate_schema(instance, schema_path: Path):
    schema = json.loads(schema_path.read_text())
    return list(Draft202012Validator(schema).iter_errors(instance))


def main() -> int:
    parser = argparse.ArgumentParser(description="SER-97 traceability foundation validator")
    parser.add_argument(
        "--profile",
        default="foundation",
        help="foundation (Stage A, default) | full (Stage B; requires SER-102 mappings)",
    )
    parser.add_argument(
        "--graph",
        type=Path,
        default=ROOT / "docs/governance/traceability/traceability.yaml",
    )
    args = parser.parse_args()
    try:
        profile = parse_profile(args.profile)
    except ValueError as exc:
        print(f"TRACE_SCHEMA {exc}", file=sys.stderr)
        return 2

    graph = load_yaml(args.graph)
    errors = validate_schema(graph, ROOT / "docs/governance/traceability/traceability.schema.json")
    if errors:
        for error in errors:
            print(f"TRACE_SCHEMA {error.message}", file=sys.stderr)
        return 2

    graph_errors = list(validate_graph(graph, profile=profile))
    if graph_errors:
        for err in graph_errors:
            print(err.format(), file=sys.stderr)
        return 3

    if args.graph.resolve() == (ROOT / "docs/governance/traceability/traceability.yaml").resolve():
        invariant_errors = list(foundation_invariants(graph))
        if invariant_errors:
            for err in invariant_errors:
                print(err.format(), file=sys.stderr)
            return 8

        tests = load_yaml(ROOT / "docs/verification/test-catalog.yaml")
        errors = validate_schema(tests, ROOT / "docs/verification/test-catalog.schema.json")
        if errors:
            for error in errors:
                print(f"TEST_SCHEMA {error.message}", file=sys.stderr)
            return 6

        design = load_yaml(ROOT / "docs/design/design-manifest.yaml")
        errors = validate_schema(design, ROOT / "docs/design/design-manifest.schema.json")
        if errors:
            for error in errors:
                print(f"DESIGN_SCHEMA {error.message}", file=sys.stderr)
            return 7

    nodes = graph["nodes"]
    req = sum(1 for n in nodes if n["nodeType"] == "REQUIREMENT" and n["status"] == "LOCKED")
    nfr = sum(1 for n in nodes if n["nodeType"] == "NFR" and n["status"] == "LOCKED")
    tests_planned = sum(1 for n in nodes if n["nodeType"] == "TEST" and n["status"] == "PLANNED")
    aliases = sum(
        1 for n in nodes if n["nodeType"] in {"REQUIREMENT", "NFR"} and n["status"] == "SUPERSEDED"
    )
    print(
        f"TRACEABILITY_PASS profile={profile.value} "
        f"req={req} nfr={nfr} tests={tests_planned} aliases={aliases}"
    )
    if profile == ValidationProfile.FULL:
        print("TRACEABILITY_FULL_PROFILE_ACTIVE", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
