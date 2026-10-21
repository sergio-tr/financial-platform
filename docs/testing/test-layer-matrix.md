# Test Layer Matrix

Status: Phase-0 implementation-blocking materialization  
Owner: SER-112 / P0-R49

| Layer | Purpose | Initial location | PR blocking |
|---|---|---|---|
| T0 | governance/static/traceability | scripts/governance + CI | yes |
| T1 | unit | backend/frontend test sources | yes |
| T2 | architecture/module | backend architecture tests | yes |
| T3 | integration | backend integration/Testcontainers | affected surfaces |
| T4 | contract/compatibility | contracts + generation diff | contract changes |
| T5 | functional E2E | frontend/e2e Playwright | affected flows |
| T6 | accessibility/visual | Playwright/a11y/visual | frontend changes |
| T7 | financial correctness | test/golden + finance suites | finance changes |
| T8 | security | security/RLS/session suites | security/data changes |
| T9 | resilience/workflow/DR | acceptance/recovery suites | workflow/ops changes |
| T10 | performance | benchmark harness | profiled/release changes |

## Rules

- Selection derives from changed surfaces plus trace graph; security, finance and contracts cannot be skipped by path heuristics alone.
- Retries are diagnostic. Persistent flakiness requires an owned quarantine with bounded expiry/removal condition.
- Normative TEST IDs live in docs/verification/test-catalog.yaml; test source may reference them, production code does not.
- CI evidence records exact commit SHA, suite/tool version, environment, fixture/golden hash and outcome.
- Automated acceptance uses repository-safe synthetic fixtures only.
- Deterministic tests pin Clock/timezone and random seeds.
- Manual acceptance never waives failed automated gates.

## Lanes

Cloud PR: T0 always; T1/T2 once implementation exists; T4 for contract surfaces; static security/dependency scans when materialized.

Trusted local acceptance: T3/T5/T6/T8 and applicable T9 inside the isolated synthetic acceptance runtime, after exact SHA/digest verification.

Release: all release-blocking normative mappings plus T7/T9/T10 where the changed surface requires them.
