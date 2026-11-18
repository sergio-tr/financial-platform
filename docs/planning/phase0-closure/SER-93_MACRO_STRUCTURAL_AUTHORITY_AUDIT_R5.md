# SER-93 §6 Macro — structural authority audit (R5-01)

Date: 2026-11-18  
Authority: Linear pack `faf775736687` (`ACCEPTED_FOR_P0_FREEZE`, SER-93 comment 2026-10-09)  
Scope: §6 Macro contract + §2 stable IDs (macro rows) + freeze amendment §C (normalized macro facts).  
Prior: R4-01 enum-vocabulary investigation → `BLOCKED_AUTHORITY` / `COMPLETED_NO_PR` (no §6.1-style closed enum unions).

This document is **non-normative evidence** for human ratification (R5 D3). It does not promote proposed semantics into Git contracts.

## Verdict

| Outcome | Detail |
|---------|--------|
| Normative `contracts/schemas/macro/**` | **BLOCKED_AUTHORITY** — insufficient closed field types, record shapes and cross-record alignment to materialize without invention |
| Gap register below | **AUTHORIZED** — consolidates exact sources, gaps and ratification options |
| Next executable Q04 slice (post-ratification) | **Human-gated** — options listed per gap; default queue remains R5-02 Research under SER-136 |

## §6 API surface (authorized names only)

| Section | API / route | Authorized operation (pack prose) | Payload detail in pack |
|---------|-------------|-----------------------------------|-------------------------|
| §6.1 | `MacroSeriesQueryApi` | `getSeriesDefinition(GetMacroSeriesQuery(macroSeriesId))`, `searchSeries` | `GetMacroSeriesQuery` fields beyond `macroSeriesId` **not enumerated**; `searchSeries` query record **absent** |
| §6.2 | `MacroObservationQueryApi` | `getObservations(MacroObservationRangeQuery(...))` | Inline query fields only: `seriesId`, `fromPeriod`, `toPeriod`, `knowledgeMode`, optional `knowledgeCutoff`, `sourcePolicyVersion` |
| §6.2 | `macro.vintage.get:v1` | `getVintage(MacroVintageQuery(...))` | `seriesId`, optional `period`, `vintageId` **or** `knowledgeCutoff` (disjunction prose, not a typed union spec) |
| §6.3 | `MacroIngestionApi` | Idempotent source batch by official revision/dataset identity + fingerprint | **No** command record, batch envelope or outcome union in §6.3 (prose constraint only) |

## View records — §6 field matrix

### `MacroSeriesView` (§6.1)

| Field | Pack constraint | Git materialization status |
|-------|-----------------|---------------------------|
| `macroSeriesId` | Implies `MacroSeriesId` (§2 String stable canonical series code) | ID pattern not duplicated in `contracts/**` for macro |
| officialDataset / dataflow / seriesKey refs | Prose only; no record shape | **GAP** — nested ref types undefined |
| title / description safe metadata | Prose only | **GAP** — no SafeText binding cited for macro-specific views |
| `frequency` | Unqualified name | **GAP** — no type; freeze §C uses `frequencyCode` (naming drift) |
| `unit` | Unqualified | **GAP** — freeze §C uses `unitCode` |
| `scale` | Unqualified | **GAP** — absent from freeze §C fact tuple |
| `seasonalAdjustment` | Unqualified | **GAP** — no closed enum or code list |
| `geography` | Unqualified | **GAP** — no registry ref |
| `sourceEvidence` / `sourceUsePolicy` | Cross-cutting §1 | **GAP** — `SourceEvidenceView` not materialized in Git schemas |
| `revisionPolicy` | Unqualified | **GAP** — no closed enum or policy record |

### `MacroObservationView` (§6.2)

| Field | Pack constraint | Git materialization status |
|-------|-----------------|---------------------------|
| `macroObservationId` | §2 `MacroObservationId` UUIDv7 | UUID v7 pattern exists (`contracts/schemas/common/uuid.v1.schema.json`) but macro-specific refs not wired |
| `seriesId` | String/code in query/view | **GAP** — naming: queries use `seriesId`, §2 defines `MacroSeriesId`; alignment prose only |
| `period` | Used in view | **GAP** — freeze §C mandates `observationPeriod` as **LocalDate** (name + type drift) |
| `exactValue` | Unqualified | **CONTRADICTION** — freeze §C `MacroObservationFact` uses `decimalValue` |
| `unit` / `scale` | Slash field in view | **GAP** — not in freeze §C observation fact |
| `status` | Unqualified | **GAP** — no enumeration |
| `publishedAt?` | Optional timestamp | Authorized optional; **instant vs date** not fixed in §6 |
| `retrievedAt` | Timestamp | Same |
| `vintageId` | Implies `MacroVintageId` UUIDv7 | ID authorized; optional in freeze fact |
| `sourceEvidence` | §1 cross-cutting | **GAP** — schema absent |
| `freshness` / `warnings` | Cross-cutting §1 | **GAP** — `FreshnessView` / `SourceWarning` not materialized |

## Stable IDs (§2, macro rows)

| ID | Pack type | Materialized |
|----|-----------|--------------|
| `MacroSeriesId` | String stable canonical series code | No dedicated pattern file |
| `MacroObservationId` | UUIDv7 | Generic UUID schema only |
| `MacroVintageId` | UUIDv7 | Generic UUID schema only |

## Freeze §C — normalized facts (post–freeze-closure authority)

```text
MacroSeriesFact(sourceRecordId, macroSeriesId, title, frequencyCode, unitCode, sourceEvidence)
MacroObservationFact(sourceRecordId, macroSeriesId, observationPeriod, decimalValue, vintageId?, publishedAt?, sourceEvidence)
observationPeriod is LocalDate.
```

| Issue | Authority A | Authority B | Classification |
|-------|-------------|-------------|----------------|
| Value field name | §6.2 view `exactValue` | §C `decimalValue` | **CONTRADICTION** — requires human pick or superseding amendment |
| Period field name | §6.2 `period` | §C `observationPeriod` | **GAP** — alias policy not stated |
| Series metadata codes | §6.1 `frequency`, `unit`, `scale`, … | §C `frequencyCode`, `unitCode` only | **GAP** — partial overlap; extra view fields lack freeze tuple fields |
| Ingestion command body | §6.3 prose | §C facts only | **GAP** — no closed `ApplyMacroSourceBatchCommand` record in inspected pack text |

## Consolidated gap register (R5 D3)

| Gap ID | Field / record / ID | Authority source | Gap type | Human ratification options (no default promotion) |
|--------|---------------------|------------------|----------|--------------------------------------------------|
| G-MACRO-001 | §6.1 closed vocabulary enums | `faf775736687` §6 | Absent authority | (a) Add §6.0 numeric/vocabulary block mirroring §4.1/§5.1 in Linear amendment; (b) Defer macro enums until cross-cutting code registries ratified; (c) Accept string codes without enums (explicit waiver) |
| G-MACRO-002 | `exactValue` vs `decimalValue` | §6.2 vs freeze §C | Contradiction | (a) Supersede §6.2 to `decimalValue`; (b) Supersede §C to `exactValue`; (c) Document canonical alias in ADR/amendment |
| G-MACRO-003 | `period` vs `observationPeriod` | §6.2 vs freeze §C | Naming drift | (a) Single canonical name in amendment; (b) JSON Schema `oneOf` alias with documented equivalence |
| G-MACRO-004 | `frequency` vs `frequencyCode` | §6.1 view vs §C fact | Naming drift | Same pattern as G-MACRO-003 |
| G-MACRO-005 | `SourceEvidenceView` embedding | §1 + §6 | Dependency | Materialize `contracts/schemas/common/source-evidence.v1` from §1 first, or macro facts remain **BLOCKED** |
| G-MACRO-006 | `knowledgeMode` on macro queries | §6.2 (+ instrument §4) | Unnamed shared enum | (a) Cross-cutting `KnowledgeMode` enum in common schema; (b) Inline const enum in macro-only slice with explicit SER-20 approval |
| G-MACRO-007 | `MacroIngestionApi` batch command | §6.3 | Record absent | Mirror market/instrument ingestion amendments (explicit command + fact union refs) before schema slice |
| G-MACRO-008 | `searchSeries` / catalog queries | §6.1 | Record absent | Define query + page types in pack amendment before AsyncAPI |
| G-MACRO-009 | Official dataset / dataflow refs | §6.1 | Record absent | Define ref records or cite external registry IDs |
| G-MACRO-010 | `macro.vintage.get:v1` disjunction | §6.2 | Constraint incomplete | Formalize `MacroVintageQuery` union (vintageId vs knowledgeCutoff rules) |

## Materialization eligibility (fail-closed)

A coherent macro Git artifact is **not** eligible in this MWU because:

1. No single authoritative record graph links §6 views, §2 IDs and §C facts without naming/type contradictions (G-MACRO-002..004).
2. Mandatory embed `SourceEvidenceView` lacks a materialized schema (G-MACRO-005).
3. Q04 precedent slices required **closed** pack definitions (enums or exact tuples); macro lacks the former and the latter is internally inconsistent across sections.

## Validation

- Pack text recovered from Linear `get_document` (`faf775736687`) on 2026-11-18.
- Cross-check: `docs/planning/phase0-closure/SER-93_INDEPENDENT_REVIEW.md` SER93-IR-001 (ellipsis / incomplete unions) — macro facts in freeze §C are **partially** closed but do not subsume §6 API records.

## Queue pointer

- **R5-02** Research §7 structural audit (separate MWU, SER-136 handoff).
- **R4-02** / macro reconciliation: only after human disposition of G-MACRO-001..010.