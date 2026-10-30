from __future__ import annotations

from typing import Any

from . import codes
from .codes import TraceError
from .id_grammar import validate_id, validate_status
from .profile import FULL_ONLY_RULES, ValidationProfile
from .relations import relation_allowed


def validate_graph(graph: dict[str, Any], *, profile: ValidationProfile) -> list[TraceError]:
    errors: list[TraceError] = []
    nodes = graph.get("nodes") or []
    edges = graph.get("edges") or []

    ids = [n["id"] for n in nodes]
    if len(ids) != len(set(ids)):
        errors.append(TraceError(codes.TRACE_DUPLICATE_ID))

    node_map = {n["id"]: n for n in nodes}
    for node in nodes:
        nid = node["id"]
        ntype = node["nodeType"]
        status = node["status"]
        if not validate_id(ntype, nid):
            errors.append(TraceError(codes.TRACE_INVALID_ID_FORMAT, f"{nid} nodeType={ntype}"))
        if not validate_status(ntype, status):
            errors.append(TraceError(codes.TRACE_INVALID_STATUS, f"{nid} status={status} nodeType={ntype}"))

    seen_edges: set[tuple[str, str, str]] = set()
    for edge in edges:
        frm = edge["from"]
        to = edge["to"]
        rel = edge["relation"]
        key = (frm, rel, to)
        if key in seen_edges:
            errors.append(TraceError(codes.TRACE_DUPLICATE_EDGE, str(edge)))
        seen_edges.add(key)

        if frm == to:
            errors.append(TraceError(codes.TRACE_SELF_EDGE, str(edge)))

        if frm not in node_map or to not in node_map:
            errors.append(TraceError(codes.TRACE_UNRESOLVED_REFERENCE, str(edge)))
            continue

        from_type = node_map[frm]["nodeType"]
        to_type = node_map[to]["nodeType"]
        if not relation_allowed(rel, from_type, to_type):
            errors.append(
                TraceError(
                    codes.TRACE_ILLEGAL_RELATION,
                    f"{frm}({from_type}) -{rel}-> {to}({to_type})",
                )
            )

        to_status = node_map[to].get("status")
        if to_status == "SUPERSEDED" and rel != "SUPERSEDES":
            has_compat = bool(edge.get("compatibilityAuthorityRef")) and bool(
                edge.get("compatibilityAuthorityRevision")
            )
            if not has_compat:
                errors.append(TraceError(codes.TRACE_SUPERSEDED_ACTIVE_REFERENCE, str(edge)))

    # SUPERSEDES acyclicity (foundation)
    supersedes_adj: dict[str, list[str]] = {}
    for edge in edges:
        if edge["relation"] != "SUPERSEDES":
            continue
        if edge["from"] not in node_map or edge["to"] not in node_map:
            continue
        supersedes_adj.setdefault(edge["from"], []).append(edge["to"])

    def _cycle(start: str) -> bool:
        stack = [start]
        seen: set[str] = set()
        while stack:
            cur = stack.pop()
            if cur in seen:
                return True
            seen.add(cur)
            stack.extend(supersedes_adj.get(cur, []))
        return False

    for src in list(supersedes_adj):
        # detect if following edges returns to src
        visited: set[str] = set()
        stack = list(supersedes_adj.get(src, []))
        while stack:
            cur = stack.pop()
            if cur == src:
                errors.append(TraceError(codes.TRACE_CARDINALITY_VIOLATION, f"SUPERSEDES cycle at {src}"))
                break
            if cur in visited:
                continue
            visited.add(cur)
            stack.extend(supersedes_adj.get(cur, []))

    legacy = graph.get("legacyExceptions") or []
    for lid in legacy:
        node = node_map.get(lid)
        if node is None:
            errors.append(TraceError(codes.TRACE_UNRESOLVED_REFERENCE, f"legacyExceptions {lid}"))
        elif node["nodeType"] != "LEGACY_EXCEPTION":
            errors.append(TraceError(codes.TRACE_ILLEGAL_RELATION, f"legacyExceptions {lid} not LEGACY_EXCEPTION"))

    if profile == ValidationProfile.FULL:
        errors.extend(_full_coverage_rules(graph, node_map))
    else:
        # Ensure FULL-only rule names remain documented/available but not executed.
        assert FULL_ONLY_RULES  # noqa: B018 — intentional design anchor for Stage B

    return errors


def _full_coverage_rules(graph: dict[str, Any], node_map: dict[str, Any]) -> list[TraceError]:
    """Stage B coverage. Requires SER-102 mappings; not used in Stage A CI."""
    errors: list[TraceError] = []
    edges = graph.get("edges") or []

    def has_rel(frm: str | None = None, rel: str | None = None, to: str | None = None, to_type: str | None = None) -> bool:
        for e in edges:
            if frm is not None and e["from"] != frm:
                continue
            if rel is not None and e["relation"] != rel:
                continue
            if to is not None and e["to"] != to:
                continue
            if to_type is not None and node_map.get(e["to"], {}).get("nodeType") != to_type:
                continue
            return True
        return False

    for node in node_map.values():
        if node["nodeType"] == "REQUIREMENT" and node["status"] == "LOCKED":
            if not has_rel(frm=None, rel="IMPLEMENTS", to=node["id"]):
                errors.append(
                    TraceError(codes.TRACE_MISSING_OWNER, f"REQUIREMENT_IMPLEMENTATION_OWNER {node['id']}")
                )
            if not has_rel(frm=None, rel="VERIFIES", to=node["id"]):
                # critical verification — full profile only
                errors.append(
                    TraceError(codes.TRACE_MISSING_VERIFICATION, f"CRITICAL_VERIFICATION_COVERAGE {node['id']}")
                )
        if node["nodeType"] == "USE_CASE" and node["status"] == "ACTIVE":
            if not has_rel(frm=node["id"], rel="IMPLEMENTS"):
                errors.append(
                    TraceError(codes.TRACE_CARDINALITY_VIOLATION, f"USE_CASE_IMPLEMENTS_REQUIREMENT {node['id']}")
                )
            if not has_rel(rel="IMPLEMENTS", to=node["id"]) and not has_rel(
                frm=None, rel="IMPLEMENTS", to=node["id"]
            ):
                # SOURCE_OWNER --IMPLEMENTS--> USE_CASE
                if not any(
                    e["relation"] == "IMPLEMENTS"
                    and e["to"] == node["id"]
                    and node_map.get(e["from"], {}).get("nodeType") == "SOURCE_OWNER"
                    for e in edges
                ):
                    errors.append(
                        TraceError(codes.TRACE_MISSING_OWNER, f"USE_CASE_SOURCE_OWNER {node['id']}")
                    )
    return errors


def foundation_invariants(graph: dict[str, Any]) -> list[TraceError]:
    """Preserve SER-101 / Phase-0 counts under SER-96 NodeTypes."""
    nodes = graph["nodes"]
    edges = graph["edges"]
    req = sum(1 for n in nodes if n["nodeType"] == "REQUIREMENT" and n["status"] == "LOCKED")
    nfr = sum(1 for n in nodes if n["nodeType"] == "NFR" and n["status"] == "LOCKED")
    tests_planned = sum(1 for n in nodes if n["nodeType"] == "TEST" and n["status"] == "PLANNED")
    aliases = sum(
        1 for n in nodes if n["nodeType"] in {"REQUIREMENT", "NFR"} and n["status"] == "SUPERSEDED"
    )
    supersedes = sum(1 for e in edges if e["relation"] == "SUPERSEDES")
    # After Stage A adjudication: 8 same-type SUPERSEDES remain (11-3 dropped).
    expected = (73, 27, 18, 9, 8)
    actual = (req, nfr, tests_planned, aliases, supersedes)
    if actual != expected:
        return [
            TraceError(
                codes.TRACE_INVARIANT,
                f"expected={expected} actual={actual}",
            )
        ]
    return []
