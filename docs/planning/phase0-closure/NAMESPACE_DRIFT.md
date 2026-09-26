# First-party namespace drift scan — 2026-09-26

Allowed first-party namespace: `sergiotr` only.

## Findings

### NS-001 — I19 `@gafi/contracts/*` and `urn:gafi:schema:`
- Location: Architecture Refinement Iteration 19 (LOCKED BASELINE EXTENSION)
- Classification: **ACTIVE_INVALID**
- Rationale: LOCKED active authority still publishes obsolete first-party `gafi` URN/alias while mission forbids any first-party namespace other than `sergiotr`.

### NS-002 — SER-108 Figma component/collection names `Gafi/*`
- Location: SER-108 Contract Pack evidence updates (`Gafi/MetricCard`, `Gafi/Primitive`, `Gafi/Foundation`, `Gafi/Semantic/Light`, `Gafi/Semantic/Dark`, `Gafi Foundations / Review Candidate`)
- Concurrent claim in SER-117 report: `Sergiotr/MetricCard` exists
- Classification: **ACTIVE_INVALID** (documented live first-party design assets still carrying obsolete namespace; SER-108 issue body itself states rename to `Sergiotr/*` is required before LOCKED)
- Note: cannot live-verify Figma in this environment (no Figma MCP). Classification based on active authority text, not invented nodes.

### NS-003 — Historical ADR / early docs without package namespace
- Git docs use `financial-platform` repo name only; no `io.gafi` in Git HEAD scan
- Classification: **HISTORICAL_ALLOWED** / N/A for first-party Java/npm namespace

### NS-004 — External references
- Eclipse Temurin, Angular, Modelina, NetworkNT, Ajv, oasdiff vendor names
- Classification: **EXTERNAL_REFERENCE**

### NS-005 — SER-58 packet `@sergiotr/contracts/*` vs I19 `@gafi/contracts/*`
- Classification: **ACTIVE_INVALID** contradiction between LOCKED I19 and SER-58 execution packet candidate (packet not yet LOCKED; I19 remains authoritative until superseded ADR)

## Freeze impact
Any ACTIVE_INVALID blocks `P0_PLANNING_FROZEN=YES` until adjudication/correction.
