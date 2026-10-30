#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
INDEX = ROOT / "docs/requirements/ser-101-materialization-index.yaml"
FUNC_README = ROOT / "docs/requirements/functional/README.md"
NFR_README = ROOT / "docs/requirements/nfr/README.md"
TRACE = ROOT / "docs/governance/traceability/traceability.yaml"

ALIASES = {
    "REQ-UI-002",
    "REQ-UI-003",
    "REQ-WF-001",
    "NFR-REL-002",
    "NFR-REL-003",
    "NFR-REL-004",
    "NFR-PRIV-001",
    "NFR-MOD-002",
    "NFR-REC-003",
}

FORBIDDEN_REALTIME = ("sergiotr.v1.commands", "sergiotr.v1.ticks", "sergiotr.v1.replies")


def fail(msg: str) -> int:
    print(f"SER101_FAIL {msg}", file=sys.stderr)
    return 2


def main() -> int:
    index = yaml.safe_load(INDEX.read_text())
    entries = index["entries"]
    ids = [e["id"] for e in entries]
    if len(ids) != 100 or len(set(ids)) != 100:
        return fail(f"expected 100 unique ids actual={len(ids)} unique={len(set(ids))}")
    req = [i for i in ids if i.startswith("REQ-")]
    nfr = [i for i in ids if i.startswith("NFR-")]
    if len(req) != 73 or len(nfr) != 27:
        return fail(f"expected 73/27 actual={len(req)}/{len(nfr)}")
    if set(index["aliases"]) != ALIASES or len(index["aliases"]) != 9:
        return fail("alias set mismatch")

    paths = []
    for e in entries:
        path = ROOT / e["path"]
        paths.append(e["path"])
        if not path.is_file():
            return fail(f"missing body {e['path']}")
        text = path.read_text()
        if e["id"] not in text.splitlines()[0]:
            return fail(f"body heading mismatch {e['path']}")
        if re.search(r"\bIMPLEMENTED\b|\bVERIFIED\b|\bDELIVERED\b", text):
            # allow negation phrases
            for line in text.splitlines():
                if re.search(r"\b(IMPLEMENTED|VERIFIED|DELIVERED)\b", line) and not re.search(
                    r"does not mark|Do not mark|not mark", line, re.I
                ):
                    return fail(f"status claim in {e['path']}: {line}")
        if re.search(r"\bgafi\b", text, re.I):
            return fail(f"forbidden namespace gafi in {e['path']}")
        for token in FORBIDDEN_REALTIME:
            if token in text:
                return fail(f"forbidden realtime token {token} in {e['path']}")
        if "Status: LOCKED" not in text:
            return fail(f"missing LOCKED status {e['path']}")
        if e["id"] in ALIASES:
            return fail(f"alias treated as active body {e['id']}")

    if len(paths) != len(set(paths)):
        return fail("duplicate canonical paths")

    # README active IDs exclude aliases section
    func_ids = re.findall(r"`(REQ-[A-Z0-9-]+)`", FUNC_README.read_text().split("## Legacy aliases")[0])
    nfr_ids = re.findall(r"`(NFR-[A-Z0-9-]+)`", NFR_README.read_text().split("## Legacy aliases")[0])
    if func_ids != req or nfr_ids != nfr:
        return fail("README active IDs diverge from materialization index")

    graph = yaml.safe_load(TRACE.read_text())
    nodes = {n["id"]: n for n in graph["nodes"]}
    for rid in ids:
        if rid not in nodes:
            return fail(f"trace missing node {rid}")
        node = nodes[rid]
        expected_type = "REQUIREMENT" if rid.startswith("REQ-") else "NFR"
        if node.get("nodeType") != expected_type:
            return fail(f"trace nodeType mismatch {rid} actual={node.get('nodeType')}")
        if node.get("status") != "LOCKED":
            return fail(f"active id not LOCKED in trace {rid}")
        doc = f"DOC:{next(e['path'] for e in entries if e['id']==rid)}"
        if doc not in nodes:
            return fail(f"trace missing DOCUMENTATION node {doc}")
        if nodes[doc].get("nodeType") != "DOCUMENTATION":
            return fail(f"trace DOC id not DOCUMENTATION {doc}")
    for alias in ALIASES:
        if alias not in nodes or nodes[alias].get("status") != "SUPERSEDED":
            return fail(f"alias not SUPERSEDED {alias}")
        # alias must not have active body file under requirements except indexes
        bodies = list((ROOT / "docs/requirements").rglob(f"{alias}.md"))
        if bodies:
            return fail(f"alias has body file {bodies}")

    ser101 = nodes.get("SER-101")
    if ser101 is None or ser101.get("nodeType") != "LINEAR_ISSUE":
        return fail("SER-101 LINEAR_ISSUE node missing")
    if ser101.get("status") not in {"ACTIVE", "DONE"}:
        return fail(f"SER-101 status unexpected {ser101.get('status')}")

    print(
        "SER101_REQUIREMENTS_PASS "
        f"req={len(req)} nfr={len(nfr)} aliases={len(ALIASES)} bodies={len(paths)}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
