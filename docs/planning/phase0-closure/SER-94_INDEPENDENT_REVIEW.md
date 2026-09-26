# SER-94 Independent READ-ONLY review — 2026-09-26

Input: SER-94 Contract Pack v1 + deterministic-finance amendment v1.1 (7ca250b8fde1).
Result: **CHANGES_REQUIRED**

## Finding IDs

### SER94-IR-001 — TransactionFactBody and type-specific monetary requirements incomplete
Severity: BLOCKING
Amendment declares sealed TransactionFactBody “whose monetary/quantity/date fields are required by transaction type” but does not enumerate per-type required fields for BUY/SELL/DEPOSIT/... An implementer must invent per-type records.

### SER94-IR-002 — Golden/independent finance vectors absent
Severity: BLOCKING for LOCKED (pack self-states)
Pack §H requires independent finance review and golden vectors (SER-116 owners) before LOCKED. No golden owners/hashes are attached to this pack. Without them, missing-data/precision semantics remain non-executable for implementation DoR even if Java shapes were complete.

### SER94-IR-003 — ValuationPolicyVersion / FxPolicyVersion / algorithm version registries not listed
Severity: BLOCKING
APIs require policy version refs (valuationPolicyVersion, fxPolicyVersion, xrayPolicyVersion, performanceMethod versions, annualizationPolicyVersion, etc.) but accepted version identifiers/contents are not enumerated. Agents would invent policy codes.

### SER94-IR-004 — Coverage/freshness/reconciliationSummary record shapes incomplete
Severity: BLOCKING
CurrentPortfolioStateView and snapshots reference coverage/freshness/reconciliationSummary without exact field-level records in this pack (unlike X-Ray CoverageSummary which is more concrete).

### SER94-IR-005 — AccountSnapshotFactBody / CashBalanceFact / PositionSnapshotFact ellipsis
Severity: BLOCKING
PortfolioNormalizedFact variants still use `...` for several bodies.

## Positive observations
- FIN_DECIMAL_V1 MathContext/scales are explicit
- Async-only application calculation boundary removes Calculated vs Accepted ambiguity
- X-Ray typed output structures and no unknown-to-zero rule are strong
- No AI calculation authority leakage observed

## Verdict
`CHANGES_REQUIRED` — not compile-ready end-to-end without inventing transaction/policy/golden bindings.
