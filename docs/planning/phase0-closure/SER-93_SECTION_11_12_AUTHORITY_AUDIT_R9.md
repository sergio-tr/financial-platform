# SER-93 — §11 idempotency/pagination and §12 mandatory contract tests (R9-01)

Date: 2026-11-20  
Authority: Linear pack `faf775736687` (`ACCEPTED_FOR_P0_FREEZE`, SER-93 comment 2026-10-09); R9 ratification SER-20 comment `abadff83-51eb-415c-9bb0-b42b85a223fb`  
Git baseline: `cb1e12ba07aa5311de137e6735c3785bc90ba357` (merge PR #37 domain-event codes)  
Scope: eligibility and authority for pack **§11** and **§12** only — contrast Linear ↔ `SER-93_COVERAGE_AUDIT_R7.md` ↔ `SER-93_SHARED_SOURCE_QUALITY_AUTHORITY_AUDIT_R8.md` ↔ `contracts/**` ↔ governance validators.  
Mode: **non-normative** audit (R9 D1). Does not promote proposals into canonical contracts or fixtures.

## Executive verdict

| Pack slice | R7 row (historical) | Git @ baseline | R9 status |
|------------|---------------------|----------------|-----------|
| §11 Idempotency and pagination | NOT_MATERIALIZED | No shared pagination/idempotency JSON Schemas; §11 prose only in Linear | **NOT_MATERIALIZED** |
| §12 Mandatory contract tests | NOT_MATERIALIZED | No SER-93 contract-test registry linkage; `docs/verification/test-catalog.yaml` has **no** SER-93 §12 mappings | **NOT_MATERIALIZED** |

Post–PR #36–#37: §8 problem codes and §9 domain-event codes are **PARTIAL** in `contracts/schemas/ser-93/` — R9 does not re-audit those slices.

**Future coherent materialization (human/SER-136 only — not approved here):**

| Candidate MWU | Eligibility | Blocker |
|---------------|-------------|---------|
| Non-normative §12 catalog index (prose → subdomain/API anchor → prerequisite schemas) | **PRIORITY_CANDIDATE** | Requires explicit human approval to treat as canonical test registry; must not invent `TEST-*` IDs |
| Shared `IdempotencyKey` / cursor pagination record types | **BLOCKED_AUTHORITY** | §11 gives rules only — no field-level record shape, bounds, or error envelope |
| Shared `SourceBatchIdentity` + fingerprint schema (§11 batch idempotency) | **BLOCKED_DEPENDENCY** | Field authority in §B / G-SHARED-001..008; R8-02 blocked |
| Executable golden/contract fixtures for any §12 line | **BLOCKED_DEPENDENCY** | View/command/fact schemas for connectivity–research largely absent; macro **BLOCKED_AUTHORITY** (G-MACRO-001..010) |

**No** single MWU can close §11 or §12 end-to-end without ratification beyond accepted pack prose.

## §11 — normative elements (authority matrix)

Pack §11 (exact prose):

```text
All source canonical-apply operations require stable source batch identity/fingerprint
and return the original terminal result on duplicate. User-triggered retryable commands
require IdempotencyKey.

Collection pagination uses opaque cursor + typed filters/sorts.
Time series uses explicit range/granularity rather than generic offsets.
```

| Element ID | Normative clause (pack) | Exact form in authority | Owner | Consumers (pack) | SER-92 | Shared §B | Macro | Fixtures / tests in Git | Status |
|------------|-------------------------|-------------------------|-------|------------------|--------|-----------|-------|-------------------------|--------|
| §11-E1 | Batch identity + fingerprint; duplicate → same terminal result | Prose + operational echoes in §3–§7 apply APIs (“stable batch identity/fingerprint”) | Each `*.api` canonical-apply / ingestion | `*CanonicalApplyApi`, `*IngestionApi`, sync apply paths | `IDENTITY_IDEMPOTENCY_CONFLICT` (SER-92 §7) is **orthogonal** — workspace/identity commands, not source batches | `SourceBatchIdentity` (`sha256` fingerprint cited R8) — **not in Git** | `MacroIngestionApi` cites batch idempotency (R5 register) — views/commands **BLOCKED** | None; R8 links idempotent batch test class to missing schema | **NOT_MATERIALIZED** |
| §11-E2 | `IdempotencyKey` on user-triggered retryable commands | Identifier name only — no VO schema, header vs body, TTL, conflict payload | Per command tables in §3–§7 | Provider binding, sync request, instrument resolve commands, etc. | SER-92 §10 transaction table defines **different** command-level idempotency rules for identity/workspace/settings | — | — | `configuration.canonical-enums` has `IdempotencyConflictCode` (SER-92 slice only) | **BLOCKED_AUTHORITY** — shape unspecified for SER-93 |
| §11-E3 | Collection pagination: opaque cursor + typed filters/sorts | Prose; partial query examples (e.g. `listCompositionHistory` “opaque cursor, not offsets”) | Query APIs per subdomain | List/search queries across connectivity, instrument, market, research | SER-92 queries “no business mutation” — pagination model **not** duplicated in SER-92 §11 (that section is **mandatory tests**, not pagination) | Filter/sort fields live in per-query records **not materialized** | Macro list queries unspecified | No cursor/token schema | **NOT_MATERIALIZED** |
| §11-E4 | Time series: explicit range/granularity, not generic offsets | Prose + query record names in §5–§6 | `market.api`, `macro.api` | `MarketSeriesQueryApi`, `MacroSeriesQueryApi`, etc. | — | — | Macro queries **BLOCKED_AUTHORITY** | No range/granularity enums beyond partial market enums | **PARTIAL_AUTHORITY** — intent clear, types absent |

### §11 cross-reference (Git vs R7/R8)

| Reference | Note |
|-----------|------|
| `SER-93_COVERAGE_AUDIT_R7.md` | §11 row NOT_MATERIALIZED — still accurate for schema/fixture materialization |
| `SER-93_SHARED_SOURCE_QUALITY_AUTHORITY_AUDIT_R8.md` | §11 batch fingerprint depends on `SourceBatchIdentity.sha256` — R8-02 **STOP** |
| `docs/governance/api-registry.md` | SER-92 contexts cite pack **§11** for **mandatory tests** (SER-92 numbering); SER-93 contexts cite **§12** for tests — numbering differs by pack |
| `contracts/schemas/configuration/canonical-enums.v1.schema.json` | `IdempotencyConflictCode` — SER-92 scope only; must not be extended for SER-93 without ownership decision |

## §12 — mandatory contract tests (16 pack lines)

Pack §12 is a **closed list of scenario assertions** (prose). No stable `TEST-*` IDs, owners, lanes, or fixture IDs are defined in the pack.

| # | Pack assertion (exact) | Primary subdomain | Pack anchor (indicative) | Shared §B / §1 deps | Macro | SER-92 / other | Git fixture / catalog | Status |
|---|------------------------|-------------------|--------------------------|---------------------|-------|----------------|----------------------|--------|
| 01 | unofficial broker connector never advertised as official | connectivity | §3 provider catalog / certification | `SourceEvidenceView`, provider views | — | Connector SPI policy (ADR) | None | **NOT_MATERIALIZED** |
| 02 | write/trade provider capability rejected | connectivity | §3 capabilities | Capability query records | — | ADR execution ban | None | **NOT_MATERIALIZED** |
| 03 | binding never contains plaintext credential | connectivity | §3 binding commands | `SafeConnectorConfigEntry`, secret vault R43 | — | REQ-CONNECTIVITY / secret policy docs | `TEST-SEC-*` PLANNED in catalog — **not** mapped to §12 | **NOT_MATERIALIZED** |
| 04 | provider disconnect preserves imported history | connectivity | §3 lifecycle | History views | — | — | None | **NOT_MATERIALIZED** |
| 05 | connector READY with stale dataset still reports stale data | connectivity | §3 + §1 `FreshnessView` | `FreshnessView` schema absent | — | — | R8 §12 implied test class | **BLOCKED_DEPENDENCY** |
| 06 | fallback records actual source + quality delta | connectivity / instrument | Quality deltas in views | `SourceEvidenceView`, completeness | — | — | None | **BLOCKED_DEPENDENCY** |
| 07 | ambiguous instrument is not auto-resolved | instrument | §4 resolution | Resolution command outcomes | — | — | None | **NOT_MATERIALIZED** |
| 08 | composition known&lt;100 preserves UNKNOWN; invalid&gt;tolerance quarantines | instrument | §4 composition | `CompletenessView`, quarantine views | — | — | R8 implied | **BLOCKED_DEPENDENCY** |
| 09 | AS_KNOWN_AT rejects late-retrieved evidence | instrument / cross | `knowledgeMode` (§4 / freeze) | Enum **not** in Git (G-COV-007) | — | — | None | **BLOCKED_DEPENDENCY** |
| 10 | wrong quote currency/convention rejected | market | §5 quotes | `MarketQuoteView`, decimal semantics | — | — | None | **NOT_MATERIALIZED** |
| 11 | FX orientation is deterministic | market | §5 FX | `FxRateFact` / views absent | — | — | None | **NOT_MATERIALIZED** |
| 12 | macro revision creates/queries vintage lineage | macro | §6 vintage | Macro views/facts | **BLOCKED** G-MACRO | — | None | **BLOCKED_AUTHORITY** |
| 13 | news full text not stored when use policy forbids it | research | §7 news / policy | `sourceUsePolicyVersion`, content views | — | — | None | **NOT_MATERIALIZED** |
| 14 | external text remains UNTRUSTED_EXTERNAL_CONTENT | research | §7 classification | `ContentAvailability` enum **partial** in Git | — | — | Partial enum only | **PARTIAL** |
| 15 | source batch duplicate is idempotent | cross apply | §11 + apply APIs | `SourceBatchIdentity` | Macro apply blocked | — | None | **BLOCKED_DEPENDENCY** |
| 16 | malformed source record quarantines without partial hidden canonical mutation | connectivity / owners | §3.6 quarantine | `QuarantineDiagnosticView`, sync summaries (SER93-IR-004) | — | — | None | **BLOCKED_DEPENDENCY** |

### §12 vs repository test infrastructure

| Artifact | SER-93 §12 linkage |
|----------|------------------|
| `docs/verification/test-catalog.yaml` | 18 entries, all **PLANNED**; owners identity/workspace/portfolio — **zero** rows cite SER-93 pack or §12 line numbers |
| `scripts/governance/run-traceability-fixtures.py` | Validates SER-92/SER-93 **code registries** only — not §12 scenarios |
| `docs/governance/api-registry.md` “Examples / tests” column | Points to Linear §12 — **index only**, no Git bodies |
| Connector TCK / ADR-0010 | Conceptual alignment; no compile-ready SER-93 contract tests |

## Consolidated gap register (R9 — additive; preserve G-COV / G-MACRO / G-SHARED)

| Gap ID | Subject | Authority source | Missing for materialization | Human action (D2) |
|--------|---------|------------------|----------------------------|-------------------|
| G-1112-001 | `IdempotencyKey` transport + persistence contract | §11 | Record shape, conflict mapping to §8 codes, relation to SER-92 `IDENTITY_IDEMPOTENCY_CONFLICT` | Ratify shared VO vs per-command fields |
| G-1112-002 | Opaque cursor pagination contract | §11 + query records | Cursor token schema, sort/filter typing | Ratify pagination MWU owner (`common/**` vs per-context) |
| G-1112-003 | Time-series range/granularity types | §11 + §5/§6 queries | Enums/value objects for granularity, bounded ranges | Approve with market/macro slices |
| G-1112-004 | §12 → `TEST-*` registry mapping | §12 | Stable IDs, owners, lanes, fixture refs | Approve catalog MWU (non-normative index first) |
| G-1112-005 | §12 executable fixtures | §12 + domains | All prerequisite view/command/fact schemas | Sequence after G-SHARED / API slices or explicit waiver |
| G-COV-001..007 | (unchanged) | R7 | — | **No change** |
| G-MACRO-001..010 | (unchanged) | R5 | — | **No change** |
| G-SHARED-001..008 | (unchanged) | R8 | — | **No change** |

## SER-96 PR body prevalidation (R9 D3 — applied to this MWU only)

Before opening the R9-01 PR, the branch author verified:

- Single `<!-- CURSOR_AGENT_PR_BODY_BEGIN -->` … `END -->` block
- `Primary: SER-93` and `Related: SER-20` only (no `SER-NNN` template residue)
- No duplicate TaskPacket / Definition of Done sections outside the delimited block
- Local: `python scripts/governance/validate-pr-trace.py` against exported body template

R9-02 (generator/prevalidation code changes) is **out of scope** for R9-01.

## Validation performed (R9-01)

| Check | Result |
|-------|--------|
| Linear `get_document` `faf775736687` §11–§12 | Recovered 2026-11-20 |
| Linear `get_document` `de3c80be0a59` §10–§11 | SER-92 §11 = mandatory **tests** (distinct from SER-93 §11) |
| `origin/main` @ `cb1e12b` | Matches user checkpoint |
| PR #26–37 slices | Not duplicated |
| `validate-traceability.py --profile foundation` | Run on branch before PR |
| `validate-task-packet.py` | Run for R9 packet |

## Audit outcome

| Outcome | Detail |
|---------|--------|
| R9-01 §11/§12 authority matrix | **AUTHORIZED** (this document) |
| Normative `contracts/**` or fixture promotion | **None** |
| Next executable slice | **STOP** — SER-136 dispatch with new ratification for G-1112-* or deferred R9-02 |