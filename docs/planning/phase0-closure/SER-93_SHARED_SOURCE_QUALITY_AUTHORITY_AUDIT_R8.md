# SER-93 — shared source-quality authority audit (R8-01)

Date: 2026-11-20  
Authority: Linear pack `faf775736687` (`ACCEPTED_FOR_P0_FREEZE`, SER-93 comment 2026-10-09)  
Git baseline: `128c35e49e362e3596dec8d36a6899201ae39afb` (merge PR #34 R7-01 coverage matrix)  
Cross-check: `docs/planning/phase0-closure/SER-93_COVERAGE_AUDIT_R7.md` (G-COV-001..007, G-MACRO-001..010 unchanged)  
Scope: pack **§1** cross-cutting prose + adjudication **§B** exact records (and freeze **§D** embeddings) vs Git; consumer and SER-92 dependency graph.  
Mode: **non-normative** authority/dependency matrix (R8 D1–D3). Does not materialize `contracts/**` or promote naming/types without human ratification.

## Executive verdict

| Question | Result |
|----------|--------|
| Field-level authority for §B record shapes in Linear? | **YES** — adjudication §B + validity constraints are explicit |
| §1 prose sufficient alone for JSON Schema? | **NO** — §B supersedes ambiguous §1 shapes for compile closure; several §1-only fields differ from §B |
| Any `contracts/schemas/**` shared slice on `main`? | **NO** — only per-domain enum schemas + `common/uuid.v1` |
| Shared contract **eligible for R8-02 materialization** without new human gate? | **NO** — `BLOCKED_AUTHORITY` (G-SHARED-001..008; G-COV-001 retained) |
| Smallest future candidate after ratification (not approved here) | Closed enums `FreshnessStatus` / `CompletenessStatus` only — still blocked on policy-version and owner-namespace decisions |

Pack §1 states each context exposes **its own** immutable quality records with a common **semantic shape**; they are **not** one shared business-kernel class. A single `contracts/schemas/common/**` bundle is therefore **not auto-authorized** without an explicit human slice decision (owner namespace vs duplicated per-context `$id`).

## Authority layering (§1 vs §B vs freeze §D)

| Layer | Pack anchor | Role in audit |
|-------|-------------|---------------|
| §1 Cross-cutting source-result semantics | Introductory prose + informal record bullets | Semantic intent; enum members for freshness/completeness match §B |
| Adjudication §B Exact source/quality records | `SourceRecordIdentity`, `SourceBatchIdentity`, `SourceEvidenceView`, `FreshnessView`, `CompletenessView`, `SafeConnectorConfigEntry` | **Compile-closure authority** for field names, types, requiredness |
| §1 only | `SourceWarning` (`code`, `safeParams`) | **Accepted prose**; not re-declared in §B; `safeParams` structure unspecified |
| Freeze §D Sync result | `SyncResultSummary`, `QuarantineDiagnosticView` | Embeds `FreshnessView`, `CompletenessView`, `SourceEvidenceView`; consumer-specific |

## Record authority matrices

### `SourceRecordIdentity` (§B)

| Field | Pack type / constraint | Git schema | Notes |
|-------|------------------------|------------|-------|
| `sourceId` | `SourceId` | — | §2: `SourceId(String stable source code)` — prose ID, no JSON Schema |
| `recordKey` | `String [1..512]` | — | |
| `revision` | `Optional<String> [1..256]` | — | |

### `SourceBatchIdentity` (§B)

| Field | Pack type / constraint | Git schema | Notes |
|-------|------------------------|------------|-------|
| `sourceId` | `SourceId` | — | |
| `batchKey` | `String [1..512]` | — | |
| `revision` | `Optional<String> [1..256]` | — | |
| `sha256` | lowercase-hex-64 | — | Batch fingerprint authority for §11 idempotency |

### `SourceEvidenceView` — §1 vs §B

| Concept | §1 field | §B field | Audit |
|---------|----------|----------|-------|
| Source | `sourceId` | `SourceId sourceId` | Aligned intent |
| Type | `sourceType` | `SourceType sourceType` | `SourceType` not a closed enum in pack — **GAP** |
| Connector | `connectorImplementationId?`, `connectorImplementationVersion?` | `Optional<ConnectorImplementationRef> connector` | **DIVERGENCE** — §B uses single ref type |
| Capability | `capabilityId?`, `capabilityVersion?` | `Optional<CapabilityRef> capability` | **DIVERGENCE** |
| Record | `sourceRecordId?`, `sourceRevision?` | `Optional<SourceRecordIdentity> sourceRecord` | **DIVERGENCE** — structured embed vs flat strings |
| Policy | `sourceUsePolicyVersion` | `SourceUsePolicyVersion usePolicyVersion` | Name drift; type not materialized in Git |
| Times | `effectiveAt?`, `publishedAt?`, `retrievedAt` | `Optional<Instant>` ×2 + `Instant retrievedAt` | `Instant` not in Git schemas |

**Consumers (pack references embedding `SourceEvidenceView` or `sourceEvidence`):** normalized facts (freeze B/C/D), `InstrumentView.identifiers[]`, composition/classification/corporate-action views, `MarketQuoteView`, `FxObservationView`, `MacroSeriesView` / observations, `NewsSearchPage`, `QuarantineDiagnosticView`, research facts — all **NOT_MATERIALIZED** in Git (per R7).

### `FreshnessView` (§1 + §B)

| Field | §1 | §B | Git |
|-------|----|----|-----|
| `status` | `FRESH \| STALE \| EXPIRED \| UNKNOWN` | `FreshnessStatus status` | **NOT_MATERIALIZED** (no enum schema) |
| `asOf` | optional | `Optional<Instant> asOf` | — |
| `age` | optional | `Optional<Duration> age` | — |
| `policyVersion` | `policyVersion` | `FreshnessPolicyVersion policyVersion` | Type ID not in Git |

**Validity (§B):** age non-negative; UNKNOWN must not fabricate `asOf`/ratio (cross-ref completeness rules).

**Consumers:** `MarketQuoteView` (`freshness`), composition (`freshness`), `SyncResultSummary`, `NewsSearchPage`, macro observations (`freshness`), sync batch summary fields (prose §3.6).

### `CompletenessView` (§1 + §B)

| Field | §1 | §B | Git |
|-------|----|----|-----|
| `status` | `COMPLETE \| PARTIAL \| UNKNOWN \| NOT_APPLICABLE` | `CompletenessStatus status` | **NOT_MATERIALIZED** |
| `knownCount` / `expectedCount` | optional | `OptionalLong` | — |
| `coverageRatio` | optional, footnote | `Optional<Ratio>` with §B constraints | `Ratio` not in Git |
| `reasonCodes` | `reasonCodes[]` | `List<SourceReasonCode> reasonCodes` | **SourceReasonCode** registry not enumerated in pack |

**Validity (§B):** counts ≥0; `coverageRatio` only if `expectedCount > 0` and ∈ [0,1]; COMPLETE requires no unresolved missing reasons.

**Consumers:** views with `completeness`, `SyncResultSummary`, sync batch completeness summary (§3.6).

### `SourceWarning` (§1 only)

| Field | Pack | Git | Audit |
|-------|------|-----|-------|
| `code` | string | — | Stable code namespace not defined in §8 for warnings (distinct from problem registry) |
| `safeParams` | unstructured | — | **BLOCKED** for schema — no shape authority |

**Consumers:** `warnings[]` on `ProviderView`, `ProviderAccountView`, `SyncOperationView`, composition/market/macro/research views (pack).

### Adjacent §B types (connectivity-safe config)

| Type | §B authority | Git | Consumer |
|------|--------------|-----|----------|
| `SafeConnectorConfigEntry` | key + `SafeConfigValue` union | — | `ProviderAccountView.nonSecretConfigurationSafeView` |
| `SafeConfigValue` | bounded unions | — | displaySafe gate — ties freeze §A `ConnectorConfigurationValues` (G-COV-002) |

### Freeze §D embeddings (shared dependency consumers)

| Type | Embedded shared fields | Materialization dependency |
|------|------------------------|----------------------------|
| `SyncResultSummary` | `CompletenessView`, `FreshnessView` | Requires shared quality records |
| `QuarantineDiagnosticView` | `SourceEvidenceView`, `FieldError` | `FieldError` from SER-92 review scope — not in Git |

## SER-92 and cross-pack dependencies

| Dependency | Owner | Pack reference | Git @ `128c35e` | Impact on shared materialization |
|------------|-------|----------------|-----------------|----------------------------------|
| `UuidV7` / UUID patterns | SER-92 §2 | SER-93 §2 stable IDs | `contracts/schemas/common/uuid.v1.schema.json` | **PARTIAL** — many SER-93 IDs are `String stable code`, not UUID |
| `PermissionCode` / AUTHZ tuples | SER-92 `AUTHZ_REGISTRY_V1` | SER-93 §10 + adjudication F | **NOT_MATERIALIZED** | **BLOCKED_DEPENDENCY** for route-level contract tests |
| `FieldError` | SER-92 pack (validation outcomes) | Quarantine / validationRejected paths | **NOT_MATERIALIZED** | Blocks `QuarantineDiagnosticView` slice |
| `Instant`, `Duration`, `Money`, `Ratio` | Java/value-type prose in packs | §B + views | **NOT_MATERIALIZED** | JSON Schema materialization needs primitive registry decision |
| `SourceId`, `SourceType` | SER-93 §2 / §B | All evidence | **NOT_MATERIALIZED** | Prerequisite for any `SourceEvidenceView` schema |
| Problem codes | SER-92 schema file | SER-93 §8 distinct codes | `contracts/schemas/ser-92/problem-codes.v1.schema.json` only | §8 SER-93 codes **NOT_MATERIALIZED** (G-COV-004) |

## Consumer dependency matrix (selected)

| Consumer (pack) | `SourceEvidenceView` | `FreshnessView` | `CompletenessView` | `SourceWarning[]` | Git status |
|-----------------|---------------------|-----------------|--------------------|--------------------|------------|
| `ProviderView` | — | — | — | yes | N |
| `ProviderAccountView` | — | — | — | — | N |
| `SyncOperationView` / `SyncBatchView` | indirect | summary | summary | yes | N |
| `SyncResultSummary` | — | embed | embed | — | N |
| `CompositionSnapshotView` | yes | yes | yes | yes | N |
| `MarketQuoteView` | yes | yes | yes | yes | N |
| `MacroObservationView` | yes | yes | — | yes | N |
| `NewsSearchPage` items | yes | yes | — | — | N |
| All normalized facts B/C/D | required embed | — | — | — | N |
| `QuarantineDiagnosticView` | yes | — | — | — | N |

**First API slice blocked on shared deps (R7 D3 reaffirmed):** e.g. `connectivity.provider.list:v1` + `ProviderView` still **BLOCKED_DEPENDENCY** on untyped `warnings[]` and optional future evidence fields on downstream views — not on `SourceEvidenceView` alone.

## Materialization eligibility (R8-02 gate)

| Candidate slice | Authority complete? | Eligible now? | Human gate |
|-----------------|---------------------|---------------|------------|
| Full §B shared quality record bundle | **NO** (type refs + §1/§B divergence) | **NO** | G-SHARED-001, 002, 005 |
| Enum-only `FreshnessStatus` + `CompletenessStatus` | **PARTIAL** (members closed in pack) | **NO** (policy version types + namespace) | G-SHARED-003, 002 |
| `SourceRecordIdentity` / `SourceBatchIdentity` alone | **PARTIAL** | **NO** (`SourceId` schema) | G-SHARED-005 |
| `SourceWarning` | **NO** (`safeParams`) | **NO** | G-SHARED-004 |
| `SafeConnectorConfigEntry` | **YES** in §B | **NO** (ties config registry G-COV-002) | G-COV-002 + G-SHARED-008 |
| `SyncResultSummary` / `QuarantineDiagnosticView` | **PARTIAL** | **NO** | Shared records + `FieldError` |

**R8-02 may proceed only after explicit human ratification** of: (1) canonical §1 vs §B field model, (2) per-context vs common schema namespace, (3) primitive/type registry prerequisites, (4) ordered slice list. Until then: **COMPLETED_NO_PR materialization** path is correct for contract work.

## Consolidated gap register (additive; prior IDs unchanged)

| Gap ID | Subject | Authority source | Missing / contradiction | Human action (not auto-promoted) |
|--------|---------|------------------|-------------------------|----------------------------------|
| G-COV-001 | Cross-cutting JSON schemas | R7 | Not in Git | **Retained** — defer or approve shared MWU |
| G-COV-002..007 | See R7 matrix | R7 | — | **No change** |
| G-MACRO-001..010 | Macro §6 | R5 | — | **No change** — BLOCKED_AUTHORITY |
| G-SHARED-001 | `SourceEvidenceView` §1 vs §B | `faf775736687` §1 + §B | Flat optional strings vs structured refs | Ratify canonical embed model for schemas |
| G-SHARED-002 | Per-context vs `common/**` namespace | §1 anti-kernel rule | Schema ownership unclear | Ratify duplication strategy vs shared `$id` |
| G-SHARED-003 | `FreshnessStatus` / `CompletenessStatus` location | §1 + §B | Not in Git | Approve enum-only MWU owner path if desired |
| G-SHARED-004 | `SourceWarning.safeParams` | §1 only | No typed map/list authority | Define safe param shape or defer warnings typing |
| G-SHARED-005 | Primitive refs (`SourceId`, `Instant`, `Duration`, `Ratio`, ref types) | §B + §2 | No JSON Schema primitives | SER-92/SER-93 primitive registry MWU or explicit waiver |
| G-SHARED-006 | `SourceReasonCode` list | §B completeness | Open code set | Enumerate or string-code policy |
| G-SHARED-007 | `SourceType` closed union | §B uses type name | No enum members in pack | Amendment or string-code policy |
| G-SHARED-008 | `SafeConnectorConfigEntry` vs config registry | §B + freeze §A | Depends on G-COV-002 | Sequence after connector config registry |

## Tests implied for a future authorized materialization MWU

| Class | Pack anchor | Prerequisite |
|-------|-------------|--------------|
| Freshness honesty | §12 — connector READY + stale dataset | `FreshnessView` schema + fixtures |
| Completeness partial composition | §12 — composition known<100% | `CompletenessView` + reason codes |
| Idempotent batch | §11 + `SourceBatchIdentity.sha256` | Batch identity schema |
| Quarantine safe diagnostics | §D — no credentials in diagnostics | `QuarantineDiagnosticView` + `FieldError` |
| Governance | TaskPacket + SER-96/97 | `validate-task-packet.py`, `validate-traceability.py --profile foundation`, `validate-pr-diff-impact.py` |

## Validation performed (R8-01)

| Check | Result |
|-------|--------|
| Linear `get_document` `faf775736687` | §1, §B, §D recovered 2026-11-20 |
| R7 matrix `SER-93_COVERAGE_AUDIT_R7.md` on `main` | Cross-cutting row NOT_MATERIALIZED — consistent |
| `contracts/schemas/**` | No freshness/completeness/source-evidence artifacts |
| `validate-traceability.py --profile foundation` | Run on branch before PR |

## Audit outcome

| Outcome | Detail |
|---------|--------|
| R8-01 authority/consumer matrix | **AUTHORIZED** (this document) |
| Normative `contracts/**` promotion | **None** |
| R8-02 shared materialization | **STOP** — human ratification required (gaps above) |