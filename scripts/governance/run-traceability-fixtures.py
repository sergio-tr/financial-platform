#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.branch_commit import (  # noqa: E402
    validate_branch,
    validate_contribution_metadata,
)
from traceability.codes import TraceError  # noqa: E402
from traceability.graph_rules import validate_graph  # noqa: E402
from traceability.pr_parser import validate_pr_body  # noqa: E402
from traceability.diff_impact import validate_phase0_diff  # noqa: E402
from traceability.profile import ValidationProfile  # noqa: E402

FIX = ROOT / "test/fixtures/governance"
SCHEMA = ROOT / "docs/governance/traceability/traceability.schema.json"


def expect_codes(errors: list[TraceError], *codes: str) -> None:
    got = {e.code for e in errors}
    for code in codes:
        if code not in got:
            raise AssertionError(f"expected {code} in {got} detail={[e.format() for e in errors]}")


def load_graph(name: str):
    return yaml.safe_load((FIX / "traceability" / name).read_text())


def main() -> int:
    # positive
    g = load_graph("positive/valid-graph.yaml")
    errs = validate_graph(g, profile=ValidationProfile.FOUNDATION)
    assert not errs, [e.format() for e in errs]

    expect_codes(
        validate_graph(load_graph("negative/duplicate-id.yaml"), profile=ValidationProfile.FOUNDATION),
        "TRACE_DUPLICATE_ID",
    )
    expect_codes(
        validate_graph(load_graph("negative/orphan-edge.yaml"), profile=ValidationProfile.FOUNDATION),
        "TRACE_UNRESOLVED_REFERENCE",
    )
    expect_codes(
        validate_graph(
            load_graph("negative/superseded-active-reference.yaml"),
            profile=ValidationProfile.FOUNDATION,
        ),
        "TRACE_SUPERSEDED_ACTIVE_REFERENCE",
    )
    expect_codes(
        validate_graph(load_graph("negative/invalid-id.yaml"), profile=ValidationProfile.FOUNDATION),
        "TRACE_INVALID_ID_FORMAT",
    )
    # invalid nodeType caught by schema in CI; graph_rules sees unknown status/id
    schema = json.loads(SCHEMA.read_text())
    invalid_type = load_graph("negative/invalid-node-type.yaml")
    assert list(Draft202012Validator(schema).iter_errors(invalid_type)), "expected schema failure for invalid nodeType"

    expect_codes(
        validate_graph(load_graph("negative/invalid-relation.yaml"), profile=ValidationProfile.FOUNDATION),
        "TRACE_ILLEGAL_RELATION",
    )
    expect_codes(
        validate_graph(load_graph("negative/invalid-pair.yaml"), profile=ValidationProfile.FOUNDATION),
        "TRACE_ILLEGAL_RELATION",
    )

    assert validate_branch("feature/docs-only")
    assert any(e.code == "TRACE_BRANCH_INVALID" for e in validate_branch("feature/docs-only"))
    assert not validate_branch("chore/SER-97-traceability-foundation")
    assert not validate_branch("cursor/ser-97-stage-b-5680")

    body_ok = (FIX / "pr/valid-body.md").read_text()
    assert not validate_pr_body(body_ok, branch_ser="SER-97")

    body_missing = (FIX / "pr/missing-primary.md").read_text()
    expect_codes(validate_pr_body(body_missing, branch_ser="SER-97"), "TRACE_PR_PRIMARY_ISSUE_MISMATCH")

    body_mismatch = (FIX / "pr/branch-mismatch.md").read_text()
    expect_codes(validate_pr_body(body_mismatch, branch_ser="SER-97"), "TRACE_PR_PRIMARY_ISSUE_MISMATCH")

    body_section = (FIX / "pr/missing-section.md").read_text()
    expect_codes(validate_pr_body(body_section, branch_ser="SER-97"), "TRACE_PR_SECTION_MISSING")

    body_wrapped = (FIX / "pr/agent-wrapped-valid.md").read_text()
    assert not validate_pr_body(body_wrapped, branch_ser="SER-97")

    body_placeholder = (FIX / "pr/agent-block-ser-nnn.md").read_text()
    expect_codes(validate_pr_body(body_placeholder, branch_ser="SER-97"), "TRACE_PR_PRIMARY_ISSUE_MISMATCH")

    body_dup_markers = (FIX / "pr/agent-duplicate-begin.md").read_text()
    expect_codes(validate_pr_body(body_dup_markers, branch_ser="SER-97"), "TRACE_PR_PRIMARY_ISSUE_MISMATCH")

    body_marker_order = (FIX / "pr/agent-end-before-begin.md").read_text()
    expect_codes(validate_pr_body(body_marker_order, branch_ser="SER-97"), "TRACE_PR_PRIMARY_ISSUE_MISMATCH")

    expect_codes(
        validate_contribution_metadata("feat: stuff\n\nCo-authored-by: Bot <bot@example.com>", check_assistant_names=True),
        "TRACE_FORBIDDEN_CONTRIBUTION_ATTRIBUTION",
    )

    # LEGACY-PR-001 node shape
    legacy = load_graph("positive/legacy-pr001.yaml")
    errs = validate_graph(legacy, profile=ValidationProfile.FOUNDATION)
    assert not errs, [e.format() for e in errs]
    assert legacy["legacyExceptions"] == ["LEGACY-PR-001"]

    assert not validate_phase0_diff(["docs/governance/execution-baseline.md"])
    assert not validate_phase0_diff(["scripts/governance/validate-pr-diff-impact.py"])
    expect_codes(
        validate_phase0_diff(["backend/src/main/App.java"]),
        "TRACE_DIFF_IMPLEMENTATION_LOCK",
    )
    expect_codes(
        validate_phase0_diff(["contracts/asyncapi/ws.yaml"]),
        "TRACE_DIFF_IMPLEMENTATION_LOCK",
    )
    assert not validate_phase0_diff(
        ["contracts/schemas/identity/user-status.v1.schema.json", "docs/contracts/README.md"]
    )
    assert not validate_phase0_diff(
        [
            "backend/application/pom.xml",
            "frontend/app/package.json",
            "docs/governance/packets/SER-140-v0-bootstrap-01.task-packet.yaml",
            "pom.xml",
        ]
    )
    expect_codes(
        validate_phase0_diff(["backend/modules/identity/application/Workspace.java"]),
        "TRACE_DIFF_IMPLEMENTATION_LOCK",
    )

    import subprocess

    ser93_codes = subprocess.run(
        [sys.executable, str(ROOT / "scripts/governance/validate-ser-93-problem-codes.py")],
        check=False,
    )
    assert ser93_codes.returncode == 0

    ser93_events = subprocess.run(
        [sys.executable, str(ROOT / "scripts/governance/validate-ser-93-domain-event-codes.py")],
        check=False,
    )
    assert ser93_events.returncode == 0

    print("TRACEABILITY_FIXTURES_PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
