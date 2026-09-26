# SER-93 Independent READ-ONLY review — 2026-09-26

Input: SER-93 Contract Pack v1 + amendment v1.1 (faf775736687).
Result: **CHANGES_REQUIRED**

## Finding IDs

### SER93-IR-001 — Normalized fact unions use ellipsis bodies
Severity: BLOCKING
Amendment §D declares:
`IdentityFact(...)`, `AliasFact(...)`, `CompositionFact(...)`, `ClassificationFact(...)`, `CorporateActionFact(...)`,
`QuoteFact(...)`, `SeriesPointFact(...)`, `FxRateFact(...)`, `BenchmarkPointFact(...)`,
`MacroSeriesFact | MacroObservationFact`, `ResearchSourceFact | NewsItemFact | ResearchEvidenceFact`
with `...` placeholders. Exact required fields, nullability, bounds and enums are not present. Compile-ready Java cannot be written without invention.

### SER93-IR-002 — ConnectorConfigurationValues schema binding unresolved
Severity: BLOCKING
CreateProviderBindingCommand requires “immutable typed ConnectorConfigurationValues” validated against “accepted capability/config schema,” but no schema IDs/versions/field sets are enumerated for any capability. Agents would invent config shapes.

### SER93-IR-003 — WeightSemantics / classification taxonomy IDs incomplete
Severity: BLOCKING for instrument composition/classification
CompositionSnapshotView.weightSemantics and ClassificationAsOfQuery.targetTaxonomyId/taxonomyVersion lack closed enums or accepted registry revision listing permitted values.

### SER93-IR-004 — Sync resultSummary / quarantine diagnostics still prose
Severity: BLOCKING
SyncOperationView.resultSummary and quarantine “safe parsed diagnostics” are not exact records.

## Positive observations
- RevalidationMode clarified as SyncMode.REVALIDATION
- SourceEvidence/Freshness/Completeness requiredness improved
- CorporateActionTerms sealed union is concrete
- No trading/write capability; no provider SDK types in API surface as declared

## Verdict
`CHANGES_REQUIRED`
