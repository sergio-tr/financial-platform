# Authority supersession — 2026-10-03

Status: planning note only. `IMPLEMENTATION_LOCK=ON`. This file does not freeze Phase 0 and does not authorize SER-70.

## What this note changes

The closure bundle merged in pull request #3 states, as if it were current, that Iteration 19 still publishes `urn:gafi:schema:` and `@gafi/contracts/*`, and that SER-58 is blocked because Iteration 19 lacks exact tool pins.

Those two statements are historical. Architecture Refinement Iteration 19 was amended on 2026-09-26. The current document uses `urn:sergiotr:schema:*` and `@sergiotr/contracts/*`, and it pins the contract-generation toolchain. The closure files under `docs/planning/phase0-closure/` stay in place as evidence of that earlier run.

## What this note does not change

Locked P0-R42 still names the fixed user destinations `gafi.v1.control`, `gafi.v1.events` and `gafi.v1.artifacts`. Locked P0-R23 still names the database roles `gafi_gateway`, `gafi_worker`, `gafi_migrator` and `gafi_backup`. Repository architecture text that repeats those locked names is left unchanged.

Later issue text in SER-104 and SER-11 uses a `sergiotr.v1` destination prefix. This note does not choose between those texts and the locked decisions. That choice remains unadjudicated.

No product code, contract schema, or financial record is added.
