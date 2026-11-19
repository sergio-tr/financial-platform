# SER-93 — integral coverage audit (R7-01)

Date: 2026-11-18  
Authority: Linear pack `faf775736687` (`ACCEPTED_FOR_P0_FREEZE`, SER-93 comment 2026-10-09)  
Git baseline: `c23f0c28cfadba9db2e7f44241fa1d3583a81a52` (merge PR #33 research §7.2 enums)  
Scope: compare accepted pack ↔ `docs/governance/api-registry.md` ↔ `contracts/**` ↔ SER-93 TaskPackets ↔ traceability evidence.  
Mode: **non-normative** audit matrix (R7 D1–D3). Does not promote proposals into canonical contracts.

## Executive verdict

| Subdomain / API owner | Pack § | Registry status | Git contract slice | Overall Q04 status |
|----------------------|--------|-----------------|-------------------|-------------------|
| Cross-cutting source quality | §1 + adjudication B | Indexed only | **NOT_MATERIALIZED** | **NOT_MATERIALIZED** |
| `connectivity.api` | §3 | PARTIAL enums | `canonical-enums.v1.schema.json` | **PARTIAL** |
| `instrument.api` | §4 | PARTIAL enums | `canonical-enums.v1.schema.json` | **PARTIAL** |
| `market.api` | §5 | PARTIAL enums | `canonical-enums.v1.schema.json` | **PARTIAL** |
| `macro.api` | §6 | BLOCKED (R5 register) | None | **BLOCKED_AUTHORITY** |
| `research.api` | §7 | PARTIAL enums | `canonical-enums.v1.schema.json` | **PARTIAL** |
| Problem registry | §8 | Indexed | **NOT_MATERIALIZED** | **NOT_MATERIALIZED** |
| Domain events | §9 | Not in registry rows | **NOT_MATERIALIZED** | **NOT_MATERIALIZED** |
| Authorization refs | §10 + freeze auth norm | Prose | **NOT_MATERIALIZED** (SER-92 AUTHZ) | **BLOCKED_DEPENDENCY** |
| Idempotency/pagination | §11 | Prose | **NOT_MATERIALIZED** | **NOT_MATERIALIZED** |
| Contract tests | §12 | Prose | **NOT_MATERIALIZED** | **NOT_MATERIALIZED** |

**No** `*.api` subdomain is **MATERIALIZED** end-to-end (records, commands, queries, outcomes, errors, AsyncAPI).  
**Macro** remains **BLOCKED_AUTHORITY** per `SER-93_MACRO_STRUCTURAL_AUTHORITY_AUDIT_R5.md` (G-MACRO-001..010).

## TaskPackets and traceability (SER-93)

| Packet | MWU | `contracts/**` touched | Validators (as recorded in packets) |
|--------|-----|------------------------|-----------------------------------|
| `SER-93-contracts-connectivity-vocabulary-v1` | Q04 connectivity enums | `connectivity/canonical-enums.v1.schema.json` | foundation + diff impact |
| `SER-93-contracts-instrument-vocabulary-v1` | Q04 instrument §4.1 | `instrument/canonical-enums.v1.schema.json` | foundation + diff impact |
| `SER-93-contracts-market-vocabulary-v1` | Q04 market §5.1 | `market/canonical-enums.v1.schema.json` | foundation + diff impact |
| `SER-93-contracts-research-vocabulary-v1` | R6-01 research §7.2 | `research/canonical-enums.v1.schema.json` | foundation + diff impact |
| `SER-93-macro-structural-authority-audit-r5` | R5-01 | none (docs only) | foundation |

Independent review `SER-93_INDEPENDENT_REVIEW.md` (2026-09-26) listed compile blockers; freeze amendments 2026-09-26 / 2026-10-09 remediated normalized-fact unions **in Linear authority** — Git still lacks corresponding schema bodies.

## Subdomain summary matrix

Legend: **M** = MATERIALIZED, **P** = PARTIAL, **B** = BLOCKED_AUTHORITY, **N** = NOT_MATERIALIZED, **D** = BLOCKED_DEPENDENCY (external owner).

| Subdomain | Enums / closed unions | View records | Commands | Query payloads | Outcome unions | §8 codes | AsyncAPI | Owner |
|-----------|----------------------|--------------|----------|----------------|----------------|----------|----------|-------|
| Cross-cutting | P (Freshness/Completeness in pack §1; not in Git) | N | — | — | — | — | N | each context (pack §1) |
| Connectivity | P | N | N | N | N | N | N | connectivity |
| Instrument | P | N | N | N | N | N | N | instrument |
| Market | P (FxPurpose §5.4 not in Git) | N | N | N | N | N | N | market |
| Macro | B | B | B | B | B | N | N | macro |
| Research | P | N | N | N | N | N | N | research |

## Cross-cutting shared dependencies (R7 D3)

Pack adjudication **§B** defines exact shapes for `SourceRecordIdentity`, `SourceBatchIdentity`, `SourceEvidenceView`, `FreshnessView`, `CompletenessView`, `SafeConnectorConfigEntry`, etc. Field-level prose is **accepted in Linear** but:

| Artifact | Pack source | Consumers (pack) | Git status | Audit note |
|----------|-------------|-------------------|------------|------------|
| `SourceEvidenceView` | §1 + §B | All views/facts | **N** | Authority exists; **do not presume sufficient** for materialization without human slice approval and consumer matrix (R7 D3) |
| `FreshnessView` / `CompletenessView` | §1 + §B | Quotes, sync, macro, news | **N** | Enum members authorized; record JSON Schema absent |
| `SourceWarning` | §1 | Warnings[] everywhere | **N** | `code` + `safeParams` not typed in Git |
| `ConnectorConfigurationValues` | Freeze A | Binding commands | **N** | Schema registry IDs not materialized (historical SER93-IR-002) |
| `SyncResultSummary` / `QuarantineDiagnosticView` | Freeze D | Sync queries | **N** | Enums partial in connectivity schema only |
| Normalized fact unions | Freeze B/C/D | Owner apply APIs | **N** | Ellipsis remediated in pack text; **no** JSON Schema bodies |
| `TaxonomyRef` + active snapshot | Freeze E | Classification | **N** | Rows authorized in pack; no Git registry artifact |
| Permission codes | Auth norm 2026-10-09 | All APIs | **D** | Canonical codes owned by SER-92 `AUTHZ_REGISTRY_V1` |

## Connectivity (`connectivity.api`) — §3

| Kind | Pack anchor | Authorized name / operation | Git artifact | Status |
|------|-------------|----------------------------|--------------|--------|
| Enum | §3.1–3.6, D/E | Certification, connection, connector op, sync mode/status, secret slot, quarantine reason, sorts | `contracts/schemas/connectivity/canonical-enums.v1.schema.json` | **P** |
| View | §3.1 | `ProviderView` | — | **N** |
| View | §3.3 | `ProviderAccountView` | — | **N** |
| View | §3.6 | `SyncOperationView`, `SyncBatchView`, quarantine view | — | **N** |
| Command | §3.2 | `CreateProviderBindingCommand`, update/disconnect/revalidate | — | **N** |
| Command | §3.5 | `RequestProviderSyncCommand`, cancel, retry-quarantine | — | **N** |
| Query | §3.1 | `ListProvidersQuery`, `GetProviderQuery` | — | **N** |
| Query | §3.4 | `ResolveConnectorCapabilityQuery` | — | **N** |
| Outcome | §3.2–3.5 | Discriminated unions per command | — | **N** |
| Use-case route | §3 | `connectivity.*:v1` (11+ routes) | — | **N** (no AsyncAPI) |

**Enum gaps (authorized in pack, not in Git schema):** `authMethodCodes`, `availabilityStatus` on `ProviderView` (unclosed in pack — **BLOCKED_AUTHORITY** for enum materialization).

## Instrument (`instrument.api`) — §4

| Kind | Pack anchor | Git artifact | Status |
|------|-------------|--------------|--------|
| Enum | §4.1 | `InstrumentType`, `InstrumentStatus`, `IdentifierNamespace` in `instrument/canonical-enums.v1.schema.json` | **P** |
| Enum | §4.5–4.7 | `knowledgeMode`, `CompositionWeightSemantics`, `CorporateActionType`, resolution `purpose` | **N** |
| View | §4.1+ | `InstrumentView`, composition/classification/corporate-action views | **N** |
| Command | §4.2, §4.8 | `ResolveInstrumentCommand`, `ApplyInstrumentSourceBatchCommand` | **N** |
| Query | §4.3–4.7 | Search/list/range queries | **N** |
| Facts | Freeze B | `Instrument*Fact` records | **N** |

## Market (`market.api`) — §5

| Kind | Pack anchor | Git artifact | Status |
|------|-------------|--------------|--------|
| Enum | §5.1 | `QuoteType`, `AdjustmentMode` | **P** |
| Enum | §5.4 | `FxObservationPurpose` (`TRANSACTION_FX` \| …) | **N** |
| View | §5.2–5.5 | `MarketQuoteView`, series, FX, benchmark views | **N** |
| Command | §5.6 | `ApplyMarketSourceBatchCommand` | **N** |
| Facts | Freeze B (market) | `QuoteFact`, `SeriesPointFact`, `FxRateFact`, `BenchmarkPointFact` | **N** |

## Macro (`macro.api`) — §6

Normative JSON Schema materialization: **BLOCKED_AUTHORITY** (see `SER-93_MACRO_STRUCTURAL_AUTHORITY_AUDIT_R5.md`, gaps G-MACRO-001..010).  
R7 does not re-open R4-01 enum investigation or duplicate R5-01 prose.

| Kind | Status |
|------|--------|
| APIs §6.1–6.3 | **B** (queries/commands underspecified vs freeze §C) |
| Views | **B** (`exactValue` vs `decimalValue`, `period` vs `observationPeriod`, etc.) |
| Facts | **N** in Git (pack freeze §C closed in Linear only) |
| Enums | **B** (no §6.1-style closed unions in pack) |

## Research (`research.api`) — §7

| Kind | Pack anchor | Git artifact | Status |
|------|-------------|--------------|--------|
| Enum | §7.2 | `ContentAvailability` in `research/canonical-enums.v1.schema.json` | **P** |
| View | §7.1–7.4 | `ResearchSource`, case views, `NewsSearchPage` items | **N** |
| Command | §7.3 | create case, add evidence, close | **N** |
| Facts | Freeze D | `ResearchSourceFact`, `NewsItemFact`, `ResearchEvidenceFact` | **N** |

## §8 Problem registry

Pack lists 33 stable `CONNECTIVITY_*` / `INSTRUMENT_*` / … codes. Git has `contracts/schemas/ser-92/problem-codes.v1.schema.json` only (SER-92 scope).  
**Status: NOT_MATERIALIZED** for SER-93 domains — requires dedicated MWU; must not extend SER-92 file without authority split decision.

## §9 Events and §12 mandatory tests

| Class | Pack § | Git | Status |
|-------|--------|-----|--------|
| Event codes (`connectivity.*`, `instrument.*`, …) | §9 | no AsyncAPI / schema | **N** |
| Mandatory contract tests prose | §12 | no fixture/test registry linkage | **N** |

## First API/subdomain “complete” materializable (R7 D1)

Under strict definition (enums + records + commands/queries + outcomes + routes + §8 codes for that API):

| Candidate | Verdict |
|-----------|---------|
| Any single `*.api` row | **None** — all remain **PARTIAL** or **BLOCKED** |
| `research.api` | Closest enum coverage depth is shallow (one enum); bodies absent |
| `connectivity.api` | Richest enum Git slice; still missing all payloads/views |

**Do not execute** a materialization MWU from this audit turn.

## Recommended next materialization priorities (human/SER-136 only)

Ordered by dependency graph (R7 D4), **not** approved here:

1. **Human disposition** on macro G-MACRO-001..010 (no schema work until ratified).
2. **Optional shared slice** — adjudication §B source-quality records: authority is field-explicit in pack, but R7 D3 requires explicit human approval before treating `SourceEvidenceView` as “sufficient” for Git (`PRIORITY_CANDIDATE`, not auto-ready).
3. **First coherent API slice** (post-shared-deps or if human defers shared): e.g. `connectivity.provider.list:v1` + `ProviderView` **only if** shared quality records materialized or pack allows staged partial (currently **BLOCKED_DEPENDENCY** on `SourceEvidenceView`).
4. **SER-93 reconciliation MWU** (future): FxPurpose, instrument cross-enums, §8 problem registry, normalized facts per owner — each as separate coherent PR (R7 D2).

## Consolidated gap register (new IDs for human ratification)

| Gap ID | Subject | Authority source | Contradiction / missing | Proposed human action |
|--------|---------|------------------|-------------------------|----------------------|
| G-COV-001 | Cross-cutting JSON schemas | Pack §B | Not in Git | Approve or defer shared-materialization MWU |
| G-COV-002 | `ConnectorConfigurationValues` + schema registry | Freeze A | SER93-IR-002 historical; pack closed in Linear | Materialize registry catalog MWU or defer |
| G-COV-003 | Normalized fact JSON schemas | Freeze B/C/D | Closed in pack, absent in Git | Per-owner fact MWU after deps |
| G-COV-004 | §8 SER-93 problem codes | §8 | Only SER-92 codes in Git | New schema namespace decision |
| G-COV-005 | AsyncAPI `connectivity.*:v1` … | §3–7 routes | No `contracts/asyncapi/**` | Post–schema reconciliation |
| G-COV-006 | Market `FxPurpose` enum | §5.4 | Not in `market/canonical-enums` | Small vocabulary PR if queued |
| G-COV-007 | Instrument cross-enums (`knowledgeMode`, corporate action, weight semantics) | §4.5–4.7, freeze B | Not in Git | Coherent instrument vocabulary extension PR |
| G-MACRO-001..010 | Macro §6 | R5 register | See R5 doc | **No change** — await human ratification |

## Validation performed (R7-01)

| Check | Result |
|-------|--------|
| Linear pack `faf775736687` fetched | Matches repo index slug |
| `api-registry.md` SER-93 rows | Aligned with Git schema paths @ `c23f0c2` |
| PR #26–33 artifacts | Not duplicated in this audit |
| `validate-traceability.py --profile foundation` | Run on branch before PR |

## Audit outcome

| Outcome | Detail |
|---------|--------|
| R7-01 coverage matrix | **AUTHORIZED** (this document) |
| Normative contract promotion | **None** |
| Next executable materialization | **STOP** — await SER-136 dispatch with explicit TaskPacket |