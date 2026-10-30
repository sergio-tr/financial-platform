from __future__ import annotations

import re

from . import codes
from .codes import TraceError

# SER-96 §10 / amendment — exact level-2 headings, once each.
REQUIRED_HEADINGS = (
    "Linear",
    "Requirements",
    "Architecture",
    "Use cases",
    "Contracts",
    "Design",
    "Tests",
    "Docs / Runbooks",
    "Security / Data impact",
    "Migrations",
)

PRIMARY_RE = re.compile(r"^Primary:\s*(SER-[1-9][0-9]*)\s*$")
RELATED_RE = re.compile(r"^Related:\s*(SER-[1-9][0-9]*(?:\s*,\s*SER-[1-9][0-9]*)*)\s*$")


def _strip_fenced(text: str) -> str:
    """Ignore fenced/indented code and HTML for machine parsing (SER-96 PR parsing)."""
    out: list[str] = []
    in_fence = False
    for line in text.splitlines():
        if line.startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        if line.startswith("    "):
            continue
        if line.lstrip().startswith("<!--"):
            continue
        out.append(line)
    return "\n".join(out)


def parse_sections(body: str) -> dict[str, list[str]]:
    cleaned = _strip_fenced(body)
    sections: dict[str, list[str]] = {}
    current: str | None = None
    for line in cleaned.splitlines():
        if line.startswith("## "):
            current = line[3:].strip()
            sections.setdefault(current, [])
            continue
        if current is not None:
            sections[current].append(line)
    return sections


def validate_pr_body(body: str, *, branch_ser: str | None = None) -> list[TraceError]:
    errors: list[TraceError] = []
    sections = parse_sections(body)

    for heading in REQUIRED_HEADINGS:
        if heading not in sections:
            errors.append(TraceError(codes.TRACE_PR_SECTION_MISSING, f"missing {heading}"))
    # Detect duplicates by recounting headings in cleaned text
    cleaned = _strip_fenced(body)
    counts: dict[str, int] = {}
    for line in cleaned.splitlines():
        if line.startswith("## "):
            h = line[3:].strip()
            counts[h] = counts.get(h, 0) + 1
    for heading in REQUIRED_HEADINGS:
        if counts.get(heading, 0) > 1:
            errors.append(TraceError(codes.TRACE_PR_SECTION_MISSING, f"duplicate {heading}"))

    linear_lines = [ln.strip() for ln in sections.get("Linear", []) if ln.strip()]
    if not linear_lines:
        errors.append(TraceError(codes.TRACE_PR_SECTION_MISSING, "missing Primary"))
        return errors

    m = PRIMARY_RE.match(linear_lines[0])
    if not m:
        errors.append(TraceError(codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH, f"invalid Primary line: {linear_lines[0]!r}"))
        return errors
    primary = m.group(1)
    related: list[str] = []
    if len(linear_lines) > 1:
        rm = RELATED_RE.match(linear_lines[1])
        if not rm:
            errors.append(TraceError(codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH, f"invalid Related line: {linear_lines[1]!r}"))
        else:
            related = [x.strip() for x in rm.group(1).split(",")]
            if primary in related:
                errors.append(TraceError(codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH, "primary listed in Related"))
            if len(related) != len(set(related)):
                errors.append(TraceError(codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH, "duplicate Related ids"))
        if len(linear_lines) > 2:
            errors.append(TraceError(codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH, "extra Linear prose"))

    if branch_ser is not None and branch_ser != primary:
        errors.append(
            TraceError(
                codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH,
                f"branch={branch_ser} primary={primary}",
            )
        )
    return errors
