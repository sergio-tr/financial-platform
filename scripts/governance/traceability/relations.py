from __future__ import annotations

# SER-96 adjudication 2026-09-26 — typed relation matrix (exact NodeType names).
# Pairs not listed are invalid. Relations do not imply their inverse.

RELATION_MATRIX: dict[str, frozenset[tuple[str, str]]] = {
    "SATISFIES": frozenset(
        {
            ("DECISION_PACK", "REQUIREMENT"),
            ("DECISION_PACK", "NFR"),
            ("ADR", "REQUIREMENT"),
            ("ADR", "NFR"),
        }
    ),
    "DERIVES_FROM": frozenset(
        {
            ("REQUIREMENT", "DECISION_PACK"),
            ("REQUIREMENT", "ADR"),
            ("NFR", "DECISION_PACK"),
            ("NFR", "ADR"),
        }
    ),
    "CONSTRAINS": frozenset(
        {
            ("DECISION_PACK", "USE_CASE"),
            ("DECISION_PACK", "SCHEMA"),
            ("DECISION_PACK", "MESSAGE"),
            ("DECISION_PACK", "ROUTE"),
            ("DECISION_PACK", "EVENT"),
            ("DECISION_PACK", "DESIGN"),
            ("DECISION_PACK", "TEST"),
            ("DECISION_PACK", "SOURCE_OWNER"),
            ("DECISION_PACK", "MIGRATION"),
            ("DECISION_PACK", "WORKFLOW_DEFINITION"),
            ("DECISION_PACK", "RELEASE_CONTROL"),
            ("ADR", "USE_CASE"),
            ("ADR", "SCHEMA"),
            ("ADR", "MESSAGE"),
            ("ADR", "ROUTE"),
            ("ADR", "EVENT"),
            ("ADR", "DESIGN"),
            ("ADR", "TEST"),
            ("ADR", "SOURCE_OWNER"),
            ("ADR", "MIGRATION"),
            ("ADR", "WORKFLOW_DEFINITION"),
            ("ADR", "RELEASE_CONTROL"),
        }
    ),
    "IMPLEMENTS": frozenset(
        {
            ("USE_CASE", "REQUIREMENT"),
            ("USE_CASE", "NFR"),
            ("SOURCE_OWNER", "USE_CASE"),
        }
    ),
    "EXPOSES": frozenset(
        {
            ("SCHEMA", "USE_CASE"),
            ("MESSAGE", "USE_CASE"),
            ("ROUTE", "USE_CASE"),
        }
    ),
    "PRODUCES": frozenset({("USE_CASE", "EVENT")}),
    "CONSUMES": frozenset({("USE_CASE", "EVENT")}),
    "VERIFIES": frozenset(
        {
            ("TEST", "REQUIREMENT"),
            ("TEST", "NFR"),
            ("TEST", "USE_CASE"),
            ("TEST", "SCHEMA"),
            ("TEST", "MESSAGE"),
            ("TEST", "ROUTE"),
            ("TEST", "EVENT"),
            ("TEST", "WORKFLOW_DEFINITION"),
            ("TEST", "MIGRATION"),
            ("TEST", "DESIGN"),
            ("TEST", "DECISION_PACK"),
            ("TEST", "ADR"),
        }
    ),
    "DESIGNS": frozenset(
        {
            ("DESIGN", "REQUIREMENT"),
            ("DESIGN", "NFR"),
            ("DESIGN", "USE_CASE"),
        }
    ),
    "OWNED_BY": frozenset(
        {
            ("SCHEMA", "SOURCE_OWNER"),
            ("MESSAGE", "SOURCE_OWNER"),
            ("ROUTE", "SOURCE_OWNER"),
            ("EVENT", "SOURCE_OWNER"),
            ("MIGRATION", "SOURCE_OWNER"),
            ("WORKFLOW_DEFINITION", "SOURCE_OWNER"),
        }
    ),
    "MIGRATES": frozenset(
        {
            ("MIGRATION", "SOURCE_OWNER"),
            ("MIGRATION", "SCHEMA"),
        }
    ),
    "RELEASES": frozenset(
        {
            ("RELEASE_CONTROL", "SOURCE_OWNER"),
            ("RELEASE_CONTROL", "SCHEMA"),
            ("RELEASE_CONTROL", "MESSAGE"),
            ("RELEASE_CONTROL", "ROUTE"),
            ("RELEASE_CONTROL", "EVENT"),
            ("RELEASE_CONTROL", "MIGRATION"),
            ("RELEASE_CONTROL", "WORKFLOW_DEFINITION"),
            ("RELEASE_CONTROL", "DOCUMENTATION"),
            ("RELEASE_CONTROL", "RUNBOOK"),
        }
    ),
}

ALL_NODE_TYPES = frozenset(
    {
        "REQUIREMENT",
        "NFR",
        "DECISION_PACK",
        "ADR",
        "USE_CASE",
        "SCHEMA",
        "MESSAGE",
        "ROUTE",
        "EVENT",
        "DESIGN",
        "TEST",
        "RUNBOOK",
        "LINEAR_ISSUE",
        "SOURCE_OWNER",
        "MIGRATION",
        "WORKFLOW_DEFINITION",
        "DOCUMENTATION",
        "RELEASE_CONTROL",
        "LEGACY_EXCEPTION",
    }
)

# DOCUMENTS: DOCUMENTATION|RUNBOOK -> any NodeType except LEGACY_EXCEPTION
RELATION_MATRIX["DOCUMENTS"] = frozenset(
    (src, dst)
    for src in ("DOCUMENTATION", "RUNBOOK")
    for dst in ALL_NODE_TYPES
    if dst != "LEGACY_EXCEPTION"
)

# SUPERSEDES: any except LEGACY_EXCEPTION -> same NodeType
RELATION_MATRIX["SUPERSEDES"] = frozenset(
    (t, t) for t in ALL_NODE_TYPES if t != "LEGACY_EXCEPTION"
)

# TRACKED_BY: any except LINEAR_ISSUE|LEGACY_EXCEPTION -> LINEAR_ISSUE
RELATION_MATRIX["TRACKED_BY"] = frozenset(
    (src, "LINEAR_ISSUE")
    for src in ALL_NODE_TYPES
    if src not in {"LINEAR_ISSUE", "LEGACY_EXCEPTION"}
)


def relation_allowed(relation: str, from_type: str, to_type: str) -> bool:
    allowed = RELATION_MATRIX.get(relation)
    if allowed is None:
        return False
    return (from_type, to_type) in allowed
