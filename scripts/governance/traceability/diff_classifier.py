from __future__ import annotations

"""
Stage A diff classifier (P0-R39 / SER-96 §9).

Classifies change *nature* from repository-relative paths. Does not infer REQ ownership.
"""

from pathlib import PurePosixPath

CLASSES = (
    "DOC_GOVERNANCE_ONLY",
    "PURE_REFACTOR",
    "OBSERVABLE_BEHAVIOR",
    "CONTRACT_CHANGE",
    "MIGRATION_CHANGE",
    "WORKFLOW_EVENT_CHANGE",
    "SECURITY_POLICY_CHANGE",
    "FINANCIAL_ALGORITHM_CHANGE",
)


def classify_paths(paths: list[str]) -> set[str]:
    if not paths:
        return set()

    classes: set[str] = set()
    only_governance = True

    for raw in paths:
        p = PurePosixPath(raw.replace("\\", "/"))
        s = p.as_posix()

        if s.startswith("contracts/") or "/asyncapi" in s or s.endswith(".asyncapi.yaml"):
            classes.add("CONTRACT_CHANGE")
            only_governance = False
        if "liquibase" in s or "/db/changelog" in s or s.startswith("infrastructure/db/"):
            classes.add("MIGRATION_CHANGE")
            only_governance = False
        if "/workflow" in s or "temporal" in s or s.startswith("docs/workflows/"):
            classes.add("WORKFLOW_EVENT_CHANGE")
            only_governance = False
        if s.startswith("docs/security/") or "/security/" in s or "rls" in s.lower():
            classes.add("SECURITY_POLICY_CHANGE")
            only_governance = False
        if ("xray" in s.lower()) or ("portfolio" in s.lower() and "calculator" in s.lower()):
            classes.add("FINANCIAL_ALGORITHM_CHANGE")
            only_governance = False
        if s.startswith("backend/") or s.startswith("frontend/"):
            classes.add("OBSERVABLE_BEHAVIOR")
            only_governance = False
        if not (
            s.startswith("docs/")
            or s.startswith(".github/")
            or s.startswith(".cursor/")
            or s.startswith("scripts/governance/")
            or s.startswith("scripts/acceptance/")
            or s == "AGENTS.md"
            or s == "MANIFEST.txt"
            or s == "README.md"
            or s.startswith("test/fixtures/governance/")
        ):
            only_governance = False

    if only_governance:
        classes.add("DOC_GOVERNANCE_ONLY")

    # PURE_REFACTOR cannot be proven from paths alone — never auto-asserted in Stage A.
    return classes
