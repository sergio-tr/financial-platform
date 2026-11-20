#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability.dev_synthetic_v1_gate import (  # noqa: E402
    allows_dev_synthetic_v1_carveout,
    is_gate_active,
    load_registry,
    path_globally_forbidden,
    path_in_vertical_slice,
    validate_registry_invariants,
)
from traceability.diff_impact import validate_phase0_diff  # noqa: E402

FIX = ROOT / "test/fixtures/governance/dev-gate"


def _test_registry_contract() -> None:
    registry = load_registry()
    assert registry["schemaVersion"] == 1
    assert registry["gateId"] == "DEV-SYNTHETIC-V1"
    assert isinstance(registry.get("verticalSlices"), list)


def _test_invariants() -> None:
    errors = validate_registry_invariants()
    assert not errors, errors


def _test_negative_unauthorized_paths() -> None:
    assert is_gate_active()
    registry = load_registry()
    blocked = [
        "backend/modules/portfolio/Calculator.java",
        "infrastructure/terraform/main.tf",
        "contracts/asyncapi/ws-v1.asyncapi.yaml",
        "design/tokens/color.json",
    ]
    for path in blocked:
        assert path_globally_forbidden(path, registry) or not allows_dev_synthetic_v1_carveout(
            [path]
        ), path
    expect_lock = validate_phase0_diff(["backend/modules/portfolio/Portfolio.java"])
    assert expect_lock, "expected IMPLEMENTATION_LOCK for unauthorized module path"


def _test_fixture_scoped_slice() -> None:
    fixture_registry = yaml.safe_load((FIX / "registry-with-identity-slice.yaml").read_text())
    assert path_in_vertical_slice(
        "backend/modules/identity/application/WorkspaceController.java",
        fixture_registry,
    )
    assert not path_in_vertical_slice(
        "backend/modules/portfolio/Portfolio.java",
        fixture_registry,
    )
    assert path_globally_forbidden(
        "infrastructure/docker/Dockerfile",
        fixture_registry,
    )


def _test_active_registry_slice_paths() -> None:
    registry = load_registry()
    slices = registry.get("verticalSlices") or []
    identity_only = [
        "backend/modules/identity/domain/WorkspaceId.java",
        "docs/governance/packets/SER-144-v1-identity-01.task-packet.yaml",
        "backend/pom.xml",
        "test/fixtures/synthetic/identity/README.md",
    ]
    if not slices:
        assert not allows_dev_synthetic_v1_carveout(identity_only)
        return
    assert allows_dev_synthetic_v1_carveout(identity_only)
    assert not allows_dev_synthetic_v1_carveout(["backend/modules/portfolio/Calculator.java"])


def main() -> int:
    _test_registry_contract()
    _test_invariants()
    _test_negative_unauthorized_paths()
    _test_fixture_scoped_slice()
    _test_active_registry_slice_paths()
    print("DEV_SYNTHETIC_V1_GATE_PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())