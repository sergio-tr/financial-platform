# SER-36 architecture spike results

`EVIDENCE_READY_FOR_ADJUDICATION`  
Reproduction label: **CLOUD_PROTOTYPE_NOT_WSL**  
executionId: `20260926T124317Z`  
base HEAD: `6669610bfdbe171cb9bbf14f7f19d63739ee8d03`

## Correctness

| Gate | Candidate A | Candidate B (harness) |
|------|-------------|------------------------|
| duplicate no double-apply | PASS | PASS |
| gap 124 no financial mutate before resync | PASS | PASS |
| atomic snapshot replace | PASS | PASS |
| stale sub after logout | PASS | PASS |
| preference optimism isolated | PASS | PASS |
| logout clears sensitive + disposes | PASS | PASS |
| single declared owner | PASS (10 entry points) | PASS (identical 10) |
| realtime outside feature store | PASS | PASS |
| deterministic inject without network | PASS | PASS |
| no forbidden storage API | PASS | PASS |

`pnpm test`: **6/6 passed** (normative scenario ×2 candidates + shared invariants).

Observed friction: Vitest+TestBed `inject()` inside `@ngrx/signals` `withProps` raised **NG0203**. Normative B correctness proven via semantic harness; `signalStore` declaration retained as candidate surface. This lowers B unit-test ergonomics / agent predictability scores (not a correctness failure of the harness scenario).

## Measurements

See `BUNDLE_MEASUREMENTS.json`.  
Angular CLI production build raw+gzip delta: **NOT_RUN** (no `ng new` app in disposable spike; packet forbids labeling NOT_RUN as PASS).

| Metric | A | B |
|--------|---|---|
| source LOC (non-empty, feature files measured) | 163 | 216 |
| runtime deps beyond Angular baseline | 0 | +`@ngrx/signals@22.0.1` |
| mutation entry points | 10 | 10 |

## Rubric (1–5) with evidence notes

| Criterion | weight | A | B | notes |
|-----------|--------|---|---|-------|
| state ownership | 12% | 5 | 5 | both single owner surfaces |
| server-authoritative normalization | 10% | 5 | 5 | financial only via snapshot/event |
| derived/computed selectors | 8% | 5 | 5 | `computed` / `withComputed` |
| replay/resync clarity | 12% | 5 | 5 | gap→RESYNC_REQUIRED identical |
| async lifecycle/cancellation | 10% | 5 | 5 | generation+dispose on logout |
| feature isolation/composability | 8% | 4 | 4 | one feature facade/store |
| unit-test ergonomics | 8% | 5 | 3 | B DI NG0203 under vitest |
| debug/devtools usefulness | 5% | 3 | 4 | B named store better |
| boilerplate/cognitive load | 8% | 5 | 3 | A fewer concepts/LOC |
| runtime/bundle dependency cost | 7% | 5 | 3 | B adds ngrx/signals |
| agent implementation predictability | 7% | 5 | 3 | B inject/context pitfalls |
| one-pattern enforceability | 5% | 4 | 4 | both enforceable |

### Weighted totals (score/5 × 100 × weight)

- Candidate A: **94.0**
- Candidate B: **82.4**
- Absolute difference: **11.6** percentage points (>5%)

## Decision rule application

1. No correctness/security failure for either candidate under normative harness.
2. Best weighted result: **Candidate A**.
3. Tie-break (≤5%) not applicable.

## Recommendation (not DECISION_LOCKED)

Prefer **A — Angular Signals + typed feature services/facades** as default state-management architecture pending explicit SER-33/ADR adjudication.

Spike code is disposable and **not** automatically production code.

## Output status

`SER36_RESULT=PASS` meaning spike executed with reproducible evidence and adjudication-ready recommendation (not automatic architecture lock).
