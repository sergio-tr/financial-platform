# Authority supersession — 2026-09-26 final freeze

Status: forward note on current `origin/main`. Historical evidence files in this directory stay unchanged.

Base inspected: `cda20b44e01224f58c215ad4ea122d4e364d9521` (merge of pull request #3).

This note does not implement product code, does not start SER-70, and does not change `IMPLEMENTATION_LOCK=ON`.

## Superseded statements in the merged closure bundle

`PHASE0_CLOSURE_REPORT.md`, `NAMESPACE_DRIFT.md`, `SER-58/PACKET_VALIDATION.md`, and `SER-58/SUMMARY.txt` say that locked Iteration 19 still publishes `urn:gafi:schema:` and `@gafi/contracts/*` as active authority.

That claim is superseded. Current Iteration 19, updated `2026-09-26T17:27:55Z`, publishes:

- `$id` pattern `urn:sergiotr:schema:<area>:<name>:v<major>`
- TypeScript package `@sergiotr/contracts`
- Java package root `io.sergiotr.financialplatform.contract.generated`

The generation path in that amendment is local Draft-07 dereference with `@apidevtools/json-schema-ref-parser` 16.0.3, then `@asyncapi/modelina` 5.10.1. `asyncapi generate models` is not the canonical generator.

## What this note does not decide

Locked P0-R42 still names `/user/exchange/amq.direct/gafi.v1.{control,events,artifacts}` and `https://gafi.home.arpa`. Locked P0-R23 still names database roles `gafi_gateway`, `gafi_worker`, `gafi_migrator`, and `gafi_backup`. The repository text added by pull request #2 follows those locked documents.

Later issue text uses `sergiotr.v1` and `sergiotr.home.arpa`. This note does not choose between those strings.

`docs/governance/architecture-manifest.md` remains the P0-R26..P0-R47 documentation cut. Its sentence that Git `main` becomes implementation authority when that pull request merges is historical for that cut. It does not absorb later planning packs and it does not replace Linear.

`P0_PLANNING_FROZEN` is not adjudicated by this file.
