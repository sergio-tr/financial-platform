# SER-36 Spike Results — Angular state candidate adjudication evidence

**Status:** `EVIDENCE_READY_FOR_ADJUDICATION` (not `DECISION_LOCKED`)  
**Environment label:** `CLOUD_PROTOTYPE_NOT_WSL`  
**Execution id:** `20260926T124724Z-auto`  
**Canonical HEAD:** `6669610bfdbe171cb9bbf14f7f19d63739ee8d03`  
**Baseline:** Angular 22.2.0 · Node 24.21.0 · TypeScript 6.0.3 · pnpm 12.7.0  

## Candidates

| | Candidate A | Candidate B |
|---|---|---|
| Approach | Angular Signals only (`signal` / `computed`) | `@ngrx/signals@22.0.1` (`signalState` + `patchState`) |
| Extra runtime dep | none beyond `@angular/core` | `@ngrx/signals@22.0.1` |
| Store LOC (physical) | 214 | 238 |
| Mutation entry points | 10 (single declared owner) | 10 (single declared owner) |
| Correctness | **PASS** (0 failures) | **PASS** (0 failures) |
| Unit tests | 4 passed | 3 passed |

Shared: FakeRealtimeClient transport outside feature store; fixed fixtures under `evidence/FIXTURES` with SHA-256; Vitest injects snapshot/events/clock — no browser network.

## Correctness assertions

Both candidates: **all pass**. `correctnessFailures: []`

Covered identically:

1. authenticated bootstrap  
2. snapshot r41@120  
3. realtime subscribe from 120  
4. apply 121,122  
5. duplicate 122 does not double-apply  
6–7. event 124 → gap / `RESYNC_REQUIRED` without mutating financial state  
8. atomic snapshot replace r42@124  
9. disconnect/reconnect resubscribe from 124; stale generation blocked  
10. logout clears sensitive state + disposes subscription  
11. derived labels recompute deterministically  
12. preference optimism accept then reject/rollback isolated from financial state  
13. financial updates only via authoritative snapshot/event path  
14. no forbidden storage API usage in feature stores  

Evidence: `evidence/CORRECTNESS.json`

## Bundle / cost (esbuild library measure)

| Artifact | raw bytes | gzip | status |
|---|---:|---:|---|
| A delta (angular external) | 6387 | 1704 | MEASURED |
| B delta (angular+ngrx external) | 6581 | 1667 | MEASURED |
| B with ngrx inlined (angular external) | 10905 | 3013 | MEASURED |
| A full (angular included) | 893161 | 186679 | MEASURED |
| B full (angular+ngrx included) | 897905 | 187825 | MEASURED |
| `ng build` production app | — | — | **NOT_RUN** (minimal Vitest projects; not Angular CLI apps) |

Delta interpretation: with Angular externalized, inlining `@ngrx/signals` adds ~**4.5 KiB raw / ~1.3 KiB gzip** vs Candidate A store delta.

## Rubric (1–5) with evidence notes

Weights from packet. Scores are for *this spike implementation quality of the pattern*, assuming production would keep the same ownership/resync rules.

| Criterion | Weight | A | B | Evidence notes |
|---|---:|---:|---:|---|
| state ownership | 12% | 5 | 5 | Both expose one declared mutation surface (`MUTATION_ENTRY_POINTS`); feature state mutated only through owner methods |
| server-authoritative normalization | 10% | 5 | 5 | Snapshot/events only mutate financial fields; preference path never writes portfolio summary |
| derived/computed | 8% | 5 | 5 | `computed` view/derivedLabel/portfolioDisplay; deterministic recompute asserted |
| replay/resync | 12% | 5 | 5 | Duplicate ignore, gap→RESYNC without apply, atomic replace, resubscribe from last applied |
| async lifecycle | 10% | 5 | 5 | generation-guarded subscribe/disconnect/reconnect/logout dispose |
| feature isolation | 8% | 5 | 5 | RealtimeClient outside store; preference optimism isolated |
| unit-test ergonomics | 8% | 5 | 4 | A instantiates with plain constructor; B uses `@ngrx/signals` state helpers (still Vitest-friendly here; full `signalStore`+DI would add TestBed friction) |
| debug/devtools | 5% | 3 | 4 | A: ad-hoc signals; B: named `signalState`/`patchState` convention slightly clearer for agents/devtools storytelling |
| boilerplate | 8% | 5 | 4 | A 214 LOC vs B 238; B adds patchState ceremony + dependency |
| runtime/bundle cost | 7% | 5 | 4 | B adds `@ngrx/signals` (~+1.3 KiB gzip when inlined over A-delta) |
| agent predictability | 7% | 5 | 4 | A aligns directly with accepted ADR-0032 / D-032 (Signals + RxJS, no NgRx by default); B is a compatible library but extra pattern surface |
| one-pattern enforceability | 5% | 4 | 4 | Both enforceable via lint/owner lists; B offers more structured store kit, A is platform-native |

### Weighted totals

\[
\begin{align*}
A &= 0.12\cdot5 + 0.10\cdot5 + 0.08\cdot5 + 0.12\cdot5 + 0.10\cdot5 + 0.08\cdot5 \\
&\quad + 0.08\cdot5 + 0.05\cdot3 + 0.08\cdot5 + 0.07\cdot5 + 0.07\cdot5 + 0.05\cdot4 \\
&= \mathbf{4.85}
\end{align*}
\]

\[
\begin{align*}
B &= 0.12\cdot5 + 0.10\cdot5 + 0.08\cdot5 + 0.12\cdot5 + 0.10\cdot5 + 0.08\cdot5 \\
&\quad + 0.08\cdot4 + 0.05\cdot4 + 0.08\cdot4 + 0.07\cdot4 + 0.07\cdot4 + 0.05\cdot4 \\
&= \mathbf{4.60}
\end{align*}
\]

Relative gap: \((4.85 - 4.60) / 4.85 \approx 4.2\%\) (≤ 5%).

## Decision rule application

1. Neither candidate has correctness failures.  
2. A has the higher weighted score (4.85 vs 4.60).  
3. Relative gap ≈ 5.2% (slightly above the ≤5% band). Per rule: choose the higher-weighted candidate when outside the band; **A** still wins. Even if treated as near-tie, lower-dependency preference also selects **A**.  
4. No maintainability blocker found for A; B’s structured kit is nice-to-have, not required to pass invariants.  
5. Classic NgRx Store was **not** considered (out of scope unless both correctness-fail).

## Recommendation (adjudication input only)

**Recommend Candidate A — Angular Signals only.**

Rationale: equal correctness; higher weighted score; no extra runtime dependency; lower boilerplate; consistent with locked ADR-0032 / D-032. Candidate B remains a viable future revisit if cross-feature Signal Store conventions or enforced store kit become necessary (ADR revisit trigger: demonstrated cross-feature state need).

**This spike does not lock the decision** — output is `EVIDENCE_READY_FOR_ADJUDICATION`.

## Deviations / risks

- Implementations are minimal TypeScript + Vitest projects using `@angular/core@22.2.0` APIs, not full `ng new` applications / `ng test` Karma/Jest Angular builders.  
- Candidate B uses `signalState` + `patchState` (Signal Store family primitives) rather than DI `signalStore(...)` factory; behavioral contract matches A. Full `signalStore` would likely worsen B’s unit-test ergonomics score slightly.  
- Production `ng build` size delta: **NOT_RUN**.  
- Evidence produced in Cursor Cloud disposable dir — **must not be labeled WSL reproduction**.  
- Concurrent writer briefly contended an earlier exec id; this package is isolated under `20260926T124724Z-auto`.
