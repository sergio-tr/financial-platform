from __future__ import annotations

from fnmatch import fnmatch
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[3]
BASELINE = ROOT / "docs/governance/execution-baseline.md"
REGISTRY = ROOT / "docs/governance/dev-synthetic-v1-carve-out-registry.yaml"

_GATE_FLAG = "DEV_SYNTHETIC_V1_GATE=SCOPED"


def _operational_state_block() -> str:
    text = BASELINE.read_text()
    start = text.find("```text")
    if start < 0:
        return text
    end = text.find("```", start + 7)
    if end < 0:
        return text[start:]
    return text[start:end]


def is_gate_active() -> bool:
    block = _operational_state_block()
    if _GATE_FLAG not in block:
        return False
    if "IMPLEMENTATION_LOCK=OFF" in block:
        return False
    if "P0_IMPLEMENTATION_READY=YES" in block:
        return False
    return True


def load_registry() -> dict:
    return yaml.safe_load(REGISTRY.read_text())


def _normalize(path: str) -> str:
    norm = path.replace("\\", "/")
    if norm.startswith("./"):
        norm = norm[2:]
    return norm


def path_matches_pattern(path: str, pattern: str) -> bool:
    norm = _normalize(path)
    pat = pattern.replace("\\", "/")
    if "**" in pat:
        return fnmatch(norm, pat) or fnmatch(norm, pat.rstrip("/"))
    if pat.endswith("/"):
        return norm.startswith(pat) or norm == pat.rstrip("/")
    return norm == pat or norm.startswith(pat + "/")


def path_globally_forbidden(path: str, registry: dict) -> bool:
    norm = _normalize(path)
    for pattern in registry.get("globalForbiddenPrefixes", []):
        if path_matches_pattern(norm, pattern):
            return True
    lowered = norm.lower()
    if "liquibase" in lowered or "/db/changelog" in lowered:
        return True
    if norm.startswith("contracts/asyncapi/"):
        return True
    return False


def path_in_vertical_slice(path: str, registry: dict) -> bool:
    norm = _normalize(path)
    for slice_ in registry.get("verticalSlices") or []:
        if slice_.get("dataClass") != "SYNTHETIC_ONLY":
            continue
        for prefix in slice_.get("allowedPathPrefixes") or []:
            if path_matches_pattern(norm, prefix):
                return True
    return False


def allows_dev_synthetic_v1_carveout(paths: list[str]) -> bool:
    """SER-143: waive OBSERVABLE_BEHAVIOR only for registry-backed vertical slices (synthetic DEV/TEST)."""
    if not paths or not is_gate_active():
        return False
    registry = load_registry()
    slices = registry.get("verticalSlices") or []
    if not slices:
        return False

    from .diff_impact import (  # noqa: PLC0415 — avoid cycle at import time
        _GOV_MATERIALIZATION_PREFIXES,
        _allows_v0_engineering_substrate_carveout,
    )

    has_slice_product_path = False
    for raw in paths:
        norm = _normalize(raw)
        if path_globally_forbidden(norm, registry):
            return False
        if any(norm == p.rstrip("/") or norm.startswith(p) for p in _GOV_MATERIALIZATION_PREFIXES):
            continue
        if norm.startswith("scripts/governance/") or norm.startswith("docs/governance/"):
            continue
        if path_in_vertical_slice(norm, registry):
            if norm.startswith(("backend/", "frontend/")):
                has_slice_product_path = True
            continue
        if _allows_v0_engineering_substrate_carveout([norm]):
            continue
        return False

    return has_slice_product_path


def validate_registry_invariants() -> list[str]:
    """Return human-readable errors; empty list means PASS."""
    errors: list[str] = []
    if not REGISTRY.is_file():
        errors.append("missing registry")
        return errors
    registry = load_registry()
    block = _operational_state_block()
    for key, expected in (registry.get("lockedInvariants") or {}).items():
        token = f"{key}={expected}"
        if token not in block:
            errors.append(f"baseline missing {token}")
    if registry.get("operationalFlag") != "SCOPED":
        errors.append("operationalFlag must be SCOPED")
    if not is_gate_active():
        errors.append(f"baseline missing {_GATE_FLAG}")
    return errors