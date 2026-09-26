# SER-108 Design audit — 2026-09-26

Status vs LOCKED requirements: **NOT_LOCKED — EVIDENCE incomplete**

## Explicit gaps for LOCKED

| Requirement | Status |
|-------------|--------|
| Namespace `Sergiotr/*` on first-party Figma assets | **OPEN / EVIDENCE_BLOCKER + ACTIVE_INVALID docs**: pack still records `Gafi/*` names; issue requires rename; this environment has no Figma MCP to re-verify live nodes |
| DES IDs | Candidates only (`DES-COMPONENT-METRIC-CARD-001`, `DES-FRONTEND-OVERVIEW-001`); grammar pending SER-96 closure; no approval → remain DRAFT |
| Figma nodes | Observed-in-pack: Foundations 0:1, Components 5:2, Overview 5:3, foundation 5:4, MetricCard 6:40, Overview EXPANDED 8:2. **Not invented here.** Live re-read: **EVIDENCE_BLOCKER** (no Figma tool) |
| DTCG | Planning tokens via SER-51 candidate; Git DTCG not materialized (post-SER-70). Figma variables documented as Gafi/* collections — namespace gap |
| EXPANDED | APPLICABLE node 8:2 (pack evidence) |
| MEDIUM | **BLOCKED** — node not created; pack cites Figma Starter MCP quota → **EVIDENCE_BLOCKER** |
| COMPACT | **BLOCKED** — same **EVIDENCE_BLOCKER** |
| Normative states | Pack defines required state keys; screen/component state coverage not fully bound with presentationNodeRef per state |
| Accessibility review | Not performed/approved; TEST-A11Y refs PLANNED only |
| Visual review | No reviewer approval recorded; TEST-VISUAL-OVERVIEW-RESPONSIVE intentionally undefined until MEDIUM+COMPACT exist |

## Classification
Missing MEDIUM/COMPACT and rename/quota issues are **EVIDENCE_BLOCKER** / tooling capacity, not new architectural decisions — except obsolete `Gafi/*` first-party naming which is also **ACTIVE_INVALID** namespace drift requiring adjudication/correction before freeze.
