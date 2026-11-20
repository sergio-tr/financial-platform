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
PLACEHOLDER_PRIMARY_RE = re.compile(r"^Primary:\s*SER-NNN\s*$", re.IGNORECASE)

_AGENT_BODY_BEGIN = "<!-- CURSOR_AGENT_PR_BODY_BEGIN -->"
_AGENT_BODY_END = "<!-- CURSOR_AGENT_PR_BODY_END -->"


def _marker_positions_outside_fence(text: str) -> tuple[list[int], list[int]]:
    begins: list[int] = []
    ends: list[int] = []
    in_fence = False
    offset = 0
    for line in text.splitlines(keepends=True):
        if line.startswith("```"):
            in_fence = not in_fence
            offset += len(line)
            continue
        if not in_fence:
            if _AGENT_BODY_BEGIN in line:
                begins.append(offset + line.index(_AGENT_BODY_BEGIN))
            if _AGENT_BODY_END in line:
                ends.append(offset + line.index(_AGENT_BODY_END))
        offset += len(line)
    return begins, ends


def validate_cursor_agent_envelope(text: str) -> list[TraceError]:
    """SER-96 / R9-02 — fail closed on Cursor agent PR wrapper markers before claim."""
    begins, ends = _marker_positions_outside_fence(text)
    if not begins and not ends:
        return []
    errors: list[TraceError] = []
    if len(begins) != 1 or len(ends) != 1:
        errors.append(
            TraceError(
                codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH,
                f"cursor agent block markers begin={len(begins)} end={len(ends)} (exactly one pair required)",
            )
        )
        return errors
    if begins[0] >= ends[0]:
        errors.append(
            TraceError(
                codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH,
                "CURSOR_AGENT_PR_BODY_END precedes BEGIN",
            )
        )
    slice_text = _canonical_pr_body(text)
    if _AGENT_BODY_BEGIN in slice_text or _AGENT_BODY_END in slice_text:
        errors.append(
            TraceError(
                codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH,
                "nested CURSOR_AGENT_PR_BODY markers inside delimited block",
            )
        )
    return errors


def _canonical_pr_body(text: str) -> str:
    """When Cursor appends a SER-96 body block, parse only that slice."""
    if _AGENT_BODY_BEGIN in text and _AGENT_BODY_END in text:
        start = text.index(_AGENT_BODY_BEGIN) + len(_AGENT_BODY_BEGIN)
        end = text.index(_AGENT_BODY_END)
        return text[start:end].strip()
    return text


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
    errors: list[TraceError] = list(validate_cursor_agent_envelope(body))
    body = _canonical_pr_body(body)
    for line in body.splitlines():
        stripped = line.strip()
        if PLACEHOLDER_PRIMARY_RE.match(stripped):
            errors.append(
                TraceError(
                    codes.TRACE_PR_PRIMARY_ISSUE_MISMATCH,
                    "placeholder Primary: SER-NNN in canonical PR body",
                )
            )
            break
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
