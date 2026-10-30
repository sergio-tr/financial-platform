from __future__ import annotations

from collections import defaultdict
from pathlib import Path
from typing import Any


MATRIX_HEADER = """# Traceability matrix (generated)

Status: GENERATED — do not hand-edit.

Profile: SER-97 Stage A foundation. Cells reflect graph edges only.
UseCase / contract / SOURCE_OWNER columns are omitted until authoritative mappings exist
(SER-102 / Stage B). Rendering gaps are absence of data, not graph statuses.
"""


def render_matrix(graph: dict[str, Any]) -> str:
    nodes = {n["id"]: n for n in graph["nodes"]}
    edges = graph["edges"]

    verifies: dict[str, list[str]] = defaultdict(list)
    documents: dict[str, list[str]] = defaultdict(list)
    designs: dict[str, list[str]] = defaultdict(list)
    tracked: dict[str, list[str]] = defaultdict(list)
    supersedes: dict[str, list[str]] = defaultdict(list)

    for e in edges:
        if e["relation"] == "VERIFIES":
            verifies[e["to"]].append(e["from"])
        elif e["relation"] == "DOCUMENTS":
            documents[e["to"]].append(e["from"])
        elif e["relation"] == "DESIGNS":
            designs[e["to"]].append(e["from"])
        elif e["relation"] == "TRACKED_BY":
            tracked[e["from"]].append(e["to"])
        elif e["relation"] == "SUPERSEDES":
            supersedes[e["from"]].append(e["to"])

    lines = [MATRIX_HEADER, "", "## REQUIREMENT / NFR", ""]
    lines.append("| id | nodeType | status | verified_by | documented_by | designed_by | tracked_by |")
    lines.append("|---|---|---|---|---|---|---|")

    req_like = [
        n
        for n in graph["nodes"]
        if n["nodeType"] in {"REQUIREMENT", "NFR"}
    ]
    req_like.sort(key=lambda n: (n["nodeType"], n["id"]))
    for n in req_like:
        nid = n["id"]
        lines.append(
            "| {id} | {nt} | {st} | {v} | {d} | {de} | {t} |".format(
                id=nid,
                nt=n["nodeType"],
                st=n["status"],
                v=", ".join(sorted(verifies.get(nid, []))) or "—",
                d=", ".join(sorted(documents.get(nid, []))) or "—",
                de=", ".join(sorted(designs.get(nid, []))) or "—",
                t=", ".join(sorted(tracked.get(nid, []))) or "—",
            )
        )

    lines.extend(["", "## SUPERSEDES", ""])
    lines.append("| from | to |")
    lines.append("|---|---|")
    for e in sorted(
        (x for x in edges if x["relation"] == "SUPERSEDES"),
        key=lambda x: (x["from"], x["to"]),
    ):
        lines.append(f"| {e['from']} | {e['to']} |")

    lines.extend(["", "## LINEAR_ISSUE", ""])
    lines.append("| id | status | title |")
    lines.append("|---|---|---|")
    for n in sorted(
        (x for x in graph["nodes"] if x["nodeType"] == "LINEAR_ISSUE"),
        key=lambda x: x["id"],
    ):
        title = (n.get("title") or "").replace("|", "/")
        lines.append(f"| {n['id']} | {n['status']} | {title} |")

    lines.extend(["", "## LEGACY_EXCEPTION", ""])
    for n in sorted(
        (x for x in graph["nodes"] if x["nodeType"] == "LEGACY_EXCEPTION"),
        key=lambda x: x["id"],
    ):
        lines.append(f"- `{n['id']}` status={n['status']} externalRef={n.get('externalRef', '')}")

    lines.append("")
    return "\n".join(lines)


def matrix_path(root: Path) -> Path:
    return root / "docs/governance/traceability/matrix/traceability-matrix.generated.md"
