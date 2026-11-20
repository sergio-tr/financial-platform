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

# SER-140 / FASTSTART V0: runnable engineering substrate only (SER-34 Iteration 13, SER-35 V0).
_V0_ENGINEERING_SUBSTRATE_PREFIXES = (
    "backend/application/",
    "backend/test-support/",
    "backend/pom.xml",
    "frontend/app/",
    "pom.xml",
    ".mvn/",
    "mvnw",
    "mvnw.cmd",
    "compose.yaml",
    "package.json",
    "pnpm-workspace.yaml",
    "pnpm-lock.yaml",
    ".node-version",
    ".java-version",
    ".gitignore",
    "scripts/bootstrap/",
    "scripts/build/",
    "scripts/contract/",
    "scripts/dev/",
    ".github/workflows/v0-engineering-substrate.yml",
)


def _allows_v0_engineering_substrate_carveout(paths: list[str]) -> bool:
    """SER-140: backend/frontend skeleton paths only, plus supporting bootstrap infra."""
    if not paths:
        return False
    has_substrate = False
    for raw in paths:
        norm = raw.replace("\\", "/")
        if any(norm == p.rstrip("/") or norm.startswith(p) for p in _V0_ENGINEERING_SUBSTRATE_PREFIXES):
            if norm.startswith(("backend/", "frontend/")):
                has_substrate = True
            continue
        if norm.startswith(
            (
                "docs/governance/packets/SER-140-v0-bootstrap-01.task-packet.yaml",
                "docs/governance/packets/SER-141-v0-bootstrap-02.task-packet.yaml",
                "docs/governance/packets/SER-142-v0-bootstrap-03.task-packet.yaml",
            )
        ):
            continue
        if any(norm == p.rstrip("/") or norm.startswith(p) for p in _GOV_MATERIALIZATION_PREFIXES):
            continue
        return False
    if has_substrate:
        return True
    # SER-142: CI-only smoke lifecycle fixes without touching app sources.
    return bool(paths) and all(
        any(norm == p.rstrip("/") or norm.startswith(p) for p in _V0_ENGINEERING_SUBSTRATE_PREFIXES)
        or norm.startswith(
            (
                "docs/governance/packets/SER-140-v0-bootstrap-01.task-packet.yaml",
                "docs/governance/packets/SER-141-v0-bootstrap-02.task-packet.yaml",
                "docs/governance/packets/SER-142-v0-bootstrap-03.task-packet.yaml",
            )
        )
        or any(norm == p.rstrip("/") or norm.startswith(p) for p in _GOV_MATERIALIZATION_PREFIXES)
        for norm in (p.replace("\\", "/") for p in paths)
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
    if blocked and "OBSERVABLE_BEHAVIOR" in blocked and _allows_v0_engineering_substrate_carveout(paths):
        blocked = [c for c in blocked if c != "OBSERVABLE_BEHAVIOR"]
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