from __future__ import annotations

import re

# SER-96 §1 — case-sensitive immutable IDs
PATTERNS: dict[str, re.Pattern[str]] = {
    "REQUIREMENT": re.compile(r"^REQ-[A-Z0-9][A-Z0-9-]*-[0-9]{3}$"),
    "NFR": re.compile(r"^NFR-[A-Z0-9][A-Z0-9-]*-[0-9]{3}$"),
    "ADR": re.compile(r"^ADR-[0-9]{4}$"),
    "DECISION_PACK": re.compile(r"^P0-R[0-9]{1,3}$"),
    "EVENT": re.compile(r"^EVT-[A-Z0-9][A-Z0-9-]*-[0-9]{3}$"),
    "DESIGN": re.compile(r"^DES-[A-Z0-9][A-Z0-9-]*-[0-9]{3}$"),
    "TEST": re.compile(
        r"^TEST-(ACC|CONTRACT|ARCH|SEC|RLS|WS|MIGR|WF|FIN|XRAY|AI|A11Y|VISUAL|DR|PERF)-[A-Z0-9][A-Z0-9-]*$"
    ),
    "RUNBOOK": re.compile(r"^RUNBOOK-[A-Z0-9][A-Z0-9-]*-[0-9]{3}$"),
    "LINEAR_ISSUE": re.compile(r"^SER-[1-9][0-9]*$"),
    "USE_CASE": re.compile(r"^[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)+:[v][1-9][0-9]*$"),
    # Materialized documentation keeps stable DOC:<repo-relative-path> ids from SER-70/SER-101.
    "DOCUMENTATION": re.compile(r"^DOC:[A-Za-z0-9._/-]+$"),
    "LEGACY_EXCEPTION": re.compile(r"^LEGACY-PR-[0-9]{3}$"),
}

# Families whose id grammar is not frozen beyond nodeType discrimination (SER-96 OPEN metadata).
OPEN_ID_FAMILIES = frozenset(
    {
        "SCHEMA",
        "MESSAGE",
        "ROUTE",
        "SOURCE_OWNER",
        "MIGRATION",
        "WORKFLOW_DEFINITION",
        "RELEASE_CONTROL",
    }
)

STATUSES: dict[str, frozenset[str]] = {
    "REQUIREMENT": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "NFR": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "DESIGN": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "ADR": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "DECISION_PACK": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "RUNBOOK": frozenset({"DRAFT", "LOCKED", "SUPERSEDED"}),
    "TEST": frozenset({"PLANNED", "IMPLEMENTED", "SUPERSEDED"}),
    "USE_CASE": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "SCHEMA": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "MESSAGE": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "ROUTE": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "EVENT": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "WORKFLOW_DEFINITION": frozenset({"ACTIVE", "DEPRECATED", "SUPERSEDED"}),
    "LINEAR_ISSUE": frozenset({"PLANNED", "ACTIVE", "DONE", "CANCELED"}),
    "SOURCE_OWNER": frozenset({"ACTIVE", "SUPERSEDED"}),
    "MIGRATION": frozenset({"ACTIVE", "SUPERSEDED"}),
    "DOCUMENTATION": frozenset({"ACTIVE", "SUPERSEDED"}),
    "RELEASE_CONTROL": frozenset({"ACTIVE", "SUPERSEDED"}),
    "LEGACY_EXCEPTION": frozenset({"ACCEPTED_EXCEPTION"}),
}


def validate_id(node_type: str, node_id: str) -> bool:
    if node_type in OPEN_ID_FAMILIES:
        return bool(node_id) and ".." not in node_id
    pattern = PATTERNS.get(node_type)
    if pattern is None:
        return False
    return pattern.fullmatch(node_id) is not None


def validate_status(node_type: str, status: str) -> bool:
    allowed = STATUSES.get(node_type)
    if allowed is None:
        return False
    return status in allowed
