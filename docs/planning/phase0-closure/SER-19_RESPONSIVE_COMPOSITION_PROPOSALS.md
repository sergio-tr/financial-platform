# SER-19 — responsive composition proposals (non-normative)

Status: **NON-NORMATIVE PROPOSAL** — planning evidence only.  
Authority: SER-20 **D5** (2026-11-17). Does **not** update `design-manifest.yaml`, `DES-FRONTEND-OVERVIEW-001`, or Figma node identifiers.  
`IMPLEMENTATION_LOCK=ON`. No Angular implementation authorized.

## Purpose

Record candidate COMPACT and MEDIUM Portfolio/Overview compositions for human review before physical Figma frames exist (`NO_NODE_ID` remains truthful in manifest).

Breakpoints (LOCKED elsewhere): COMPACT <768px, MEDIUM 768–1199px, EXPANDED ≥1200px (`design/tokens/dimension.json` `layout.breakpoint.shell.*`). Reference frames from SER-51 candidate: COMPACT 390×844, MEDIUM 834×1112, EXPANDED 1440×1024.

## Proposal 1 — COMPACT shell

**Navigation:** fixed **bottom navigation** with primary destinations: **Home (Overview)**, **Portfolio**, **X-Ray**, **More** (Research, Workflows, Settings behind More). Assistant entry via floating action or More — not a fifth bottom tab in v1 proposal.

**Overview content stack (top → bottom):**

1. Portfolio value + period return (`Sergiotr/MetricCard` pattern).
2. **Performance chart** (semantic dataset; chart library per ADR-0014).
3. **X-Ray summary** strip (coverage/freshness/unknown indicators; non-color-only states).
4. Positions preview (scroll continues).

**Behavior constraints (aligns with REQ-FRONTEND-001 intent, not additional AC):**

- Material values and permission-denied states remain reachable without desktop-only hover.
- Stale/unknown/error states use text/badge semantics from tokens.

## Proposal 2 — MEDIUM shell

**Chrome:** persistent **left navigation rail** (icons + labels); width aligned to `layout.sidebar.expanded` token (240px proposal).

**Overview layout:** **70/30 horizontal split** (main / secondary):

| Zone | ~Share | Content |
|------|--------|---------|
| Main (left) | **70%** | Portfolio value header, large performance chart, positions table (primary). |
| Secondary (right) | **30%** | X-Ray overview, allocation/risk summary cards, workflow/AI status snippets. |

**Density:** gutters per `layout.medium.gutter` (24px). Touch targets respect `size.touch.minimum`.

## Explicit non-goals

- No Figma `nodeId` assignments or manifest `observedNodes` entries.
- No change to `approval: NOT_RECORDED` or `FIGMA_EVIDENCE_PENDING`.
- Not input to `TEST-VISUAL-OVERVIEW-RESPONSIVE` until reviewed frames exist for each class.
- EXPANDED composition remains represented only by existing manifest node `8:2` (unchanged).

## Promotion path (human)

1. Designer publishes COMPACT and MEDIUM frames in file `vNGRqJpCWQ3FpWjC1nwvpU` with stable `Sergiotr/*` naming.
2. Update `docs/design/design-manifest.yaml` with real `nodeId` values (remove `NO_NODE_ID`).
3. Independent visual/a11y review; record approval per SER-108 process.
4. Optionally adopt or revise this proposal document; normative status lives in manifest + DES records only after approval.

## References

- `docs/planning/phase0-closure/SER-20_AUTHORITY_RATIFICATION_2026-11-17.md` (D5)
- `docs/planning/phase0-closure/SER-108_AUDIT.md`
- `docs/ux/dashboard-system.md`
- `design/tokens/**` (SER-51 foundation)