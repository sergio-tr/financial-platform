from __future__ import annotations

from . import codes
from .codes import TraceError
from .diff_classifier import classify_paths

# Product / implementation surfaces forbidden while IMPLEMENTATION_LOCK=ON (Phase 0 materialization).
_PHASE0_FORBIDDEN_CLASSES = frozenset(
    {
        "OBSERVABLE_BEHAVIOR",
        "CONTRACT_CHANGE",
        "MIGRATION_CHANGE",
        "FINANCIAL_ALGORITHM_CHANGE",
    }
)

_CONTRACT_SCHEMA_MATERIALIZATION_PREFIX = "contracts/schemas/"

_GOV_MATERIALIZATION_PREFIXES = (
    "docs/",
    ".github/",
    "scripts/governance/",
    "MANIFEST.txt",
    "AGENTS.md",
    "README.md",
)


def _allows_contract_materialization_carveout(paths: list[str]) -> bool:
    """SER-92..95 / Q04: accepted-pack AsyncAPI+JSON Schema bodies only (SER-20 D1)."""
    if not paths:
        return False
    has_schema = False
    for raw in paths:
        norm = raw.replace("\\", "/")
        if norm.startswith(_CONTRACT_SCHEMA_MATERIALIZATION_PREFIX):
            has_schema = True
            continue
        if norm.startswith("contracts/"):
            return False
        if any(norm == p.rstrip("/") or norm.startswith(p) for p in _GOV_MATERIALIZATION_PREFIXES):
            continue
        return False
    return has_schema


def validate_phase0_diff(changed_paths: list[str]) -> list[TraceError]:
    """Classify PR diff paths and fail closed on implementation-lock violations."""
    if not changed_paths:
        return []

    paths = [p.strip() for p in changed_paths if p.strip()]
    if not paths:
        return []

    classes = classify_paths(paths)
    errors: list[TraceError] = []

    blocked = sorted(classes & _PHASE0_FORBIDDEN_CLASSES)
    if blocked and "CONTRACT_CHANGE" in blocked and _allows_contract_materialization_carveout(paths):
        blocked = [c for c in blocked if c != "CONTRACT_CHANGE"]
    if blocked:
        errors.append(
            TraceError(
                codes.TRACE_DIFF_IMPLEMENTATION_LOCK,
                f"forbidden_classes={blocked}",
            )
        )

    for raw in paths:
        s = raw.replace("\\", "/")
        if s.startswith(("docs/", ".github/", "scripts/governance/")):
            continue
        if "workflow" in s.lower() or "temporal" in s.lower():
            errors.append(
                TraceError(
                    codes.TRACE_DIFF_IMPLEMENTATION_LOCK,
                    f"non-governance workflow path {s}",
                )
            )

    return errors