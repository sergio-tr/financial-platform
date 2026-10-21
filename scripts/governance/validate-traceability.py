#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path
import sys

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]

def load_yaml(path: Path):
    return yaml.safe_load(path.read_text())

def validate(instance, schema_path: Path):
    schema = json.loads(schema_path.read_text())
    return list(Draft202012Validator(schema).iter_errors(instance))

def main() -> int:
    graph = load_yaml(ROOT / "docs/governance/traceability/traceability.yaml")
    errors = validate(graph, ROOT / "docs/governance/traceability/traceability.schema.json")
    if errors:
        for error in errors:
            print(f"TRACE_SCHEMA {error.message}", file=sys.stderr)
        return 2

    nodes = graph["nodes"]
    ids = [node["id"] for node in nodes]
    if len(ids) != len(set(ids)):
        print("TRACE_DUPLICATE_ID", file=sys.stderr)
        return 3
    node_map = {node["id"]: node for node in nodes}

    for edge in graph["edges"]:
        if edge["from"] not in node_map or edge["to"] not in node_map:
            print(f"TRACE_ORPHAN_EDGE {edge}", file=sys.stderr)
            return 4
        if node_map[edge["to"]].get("status") == "SUPERSEDED" and edge["relation"] != "supersedes":
            print(f"TRACE_ACTIVE_TO_SUPERSEDED {edge}", file=sys.stderr)
            return 5

    tests = load_yaml(ROOT / "docs/verification/test-catalog.yaml")
    errors = validate(tests, ROOT / "docs/verification/test-catalog.schema.json")
    if errors:
        for error in errors:
            print(f"TEST_SCHEMA {error.message}", file=sys.stderr)
        return 6

    design = load_yaml(ROOT / "docs/design/design-manifest.yaml")
    errors = validate(design, ROOT / "docs/design/design-manifest.schema.json")
    if errors:
        for error in errors:
            print(f"DESIGN_SCHEMA {error.message}", file=sys.stderr)
        return 7

    req = sum(1 for n in nodes if n["type"] == "REQ" and n.get("status") == "LOCKED")
    nfr = sum(1 for n in nodes if n["type"] == "NFR" and n.get("status") == "LOCKED")
    tests_planned = sum(1 for n in nodes if n["type"] == "TEST" and n.get("status") == "PLANNED")
    aliases = sum(1 for n in nodes if n["type"] in {"REQ", "NFR"} and n.get("status") == "SUPERSEDED")
    supersedes = sum(1 for e in graph["edges"] if e["relation"] == "supersedes")
    expected = (73, 27, 18, 9, 10)
    actual = (req, nfr, tests_planned, aliases, supersedes)
    if actual != expected:
        print(f"TRACE_INVARIANT expected={expected} actual={actual}", file=sys.stderr)
        return 8

    print(f"TRACEABILITY_PASS req={req} nfr={nfr} tests={tests_planned} aliases={aliases} supersedes={supersedes}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
