# DEV synthetic V1 gate (SER-143)

Status: **SCOPED** (authorized by SER-143; does not unlock Phase 0)

## Purpose

While `IMPLEMENTATION_LOCK=ON` and `P0_IMPLEMENTATION_READY=NO` remain unchanged, this gate authorizes **small, isolated DEV/TEST functional code iterations** only when:

1. A human-selected ticket owns an explicit TaskPacket with `taskKind: IMPLEMENTATION` (or future V1 vertical packets).
2. Changed paths are a subset of that packet’s `allowedPaths` and the active slice in `dev-synthetic-v1-carve-out-registry.yaml`.
3. Data is **synthetic only** (`dataClass: SYNTHETIC_ONLY`); no production, provider credentials, real portfolio exports, or live financial truth.
4. Each vertical MWU ships with tests, governance CI, SER-96 preclaim, and **manual** squash merge after human approval.

This gate **does not**:

- Set `P0_IMPLEMENTATION_READY=YES` or `IMPLEMENTATION_LOCK=OFF`.
- Replace SER-98 adversarial audit or SER-20 final readiness.
- Authorize AIS (SER-139), blanket `backend/**`, migrations, AsyncAPI product bodies, or provider smoke without a dedicated ratified MWU.

## Operational flag

```text
DEV_SYNTHETIC_V1_GATE=SCOPED
```

Recorded in `docs/governance/execution-baseline.md`. V0 engineering substrate (SER-140..142) carve-out remains separate.

## Enforcement

- `scripts/governance/traceability/dev_synthetic_v1_gate.py` — registry + path rules.
- `scripts/governance/validate-dev-synthetic-v1-gate.py` — CI invariant checks and fixture-driven negative/positive path tests.
- `scripts/governance/traceability/diff_impact.py` — waives `OBSERVABLE_BEHAVIOR` only when the dev gate and an **active registry vertical slice** both permit the diff (empty `verticalSlices` = no V1 product paths yet).

## Active vertical slices

| Slice ID | TaskPacket | Status |
|----------|------------|--------|
| `V1-IDENTITY-WORKSPACE` | `docs/governance/packets/SER-144-v1-identity-01.task-packet.yaml` | SER-144 synthetic Identity+Workspace authorization (DEV/TEST only) |