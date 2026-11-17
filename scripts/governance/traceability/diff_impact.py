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
    if blocked:
        errors.append(
            TraceError(
                codes.TRACE_DIFF_IMPLEMENTATION_LOCK,
                f"forbidden_classes={blocked}",
            )
        )

    for raw in paths:
        s = raw.replace("\\", "/")
        if s.startswith("docs/"):
            continue
        if "workflow" in s.lower() or "temporal" in s.lower():
            errors.append(
                TraceError(
                    codes.TRACE_DIFF_IMPLEMENTATION_LOCK,
                    f"non-doc workflow path {s}",
                )
            )

    return errors