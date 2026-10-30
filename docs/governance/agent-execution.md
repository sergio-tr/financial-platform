# Agent Execution Firewall

Status: Phase-0 implementation-blocking materialization  
Owners: SER-115 / P0-R52  
Amended by: P0-R59 / SER-132 (manual Cursor execution baseline)

## Purpose

Implementation agents execute only a validated TaskPacket v1. Conversation text, Slack history and agent suggestions cannot widen the packet.

Execution mode is manual Cursor only. See `docs/governance/execution-baseline.md`.

## Entry envelope

```text
EXECUTE_TASK
Task: SER-<id>
Packet: <canonical path>
PacketHash: <sha256>
BaseRevision: <git sha>
Mode: PLAN | IMPLEMENT | REVIEW | DEBUG
```

The packet routes authority; it does not replace canonical requirements, ADRs, contracts, DES or TEST records.

Sergio must explicitly select the task and explicitly start the run. Agents must not self-select, auto-advance, or recursively dispatch.

## Preconditions

Before any repository edit:

1. validate the packet against `docs/governance/task-packet.schema.json`;
2. verify `packetHash` against canonical packet semantics;
3. require `baseRevision` to equal the current candidate base;
4. resolve every referenced active ID from Git-local authority;
5. reject SUPERSEDED references except explicit historical evidence;
6. require non-empty `allowedPaths` and `forbiddenPaths`;
7. require explicit `NOT_APPLICABLE` for semantic fields that do not apply;
8. reject unresolved assumptions for security, finance, public contracts, migrations and workflow semantics.

## Pre-implementation outcomes

Only:

- `READY_TO_IMPLEMENT`
- `BLOCKED_DECISION`
- `INVALID_TASK_PACKET`

Blocked/invalid outcomes perform no product edits.

## Risk

- LOW: documentation/refactor with no observable behavior.
- MEDIUM: bounded observable behavior with accepted contracts.
- SENSITIVE: security, privacy, persistence, external-data or workflow effects.
- CRITICAL: financial algorithms, destructive migrations, secrets/keys or release/DR controls.

SENSITIVE/CRITICAL IMPLEMENT requires a current PLAN for the same packet hash/base revision. CRITICAL also requires an independent read-only REVIEW.

## Scope and concurrency

Agents may write only under `allowedPaths`. Attempts to widen paths, weaken verification, change frozen architecture or bypass a blocked dependency invalidate the run.

Immediately before mutation compare current base revision with the packet. Drift invalidates the packet; never silently rebase and continue.

Execution venues are LOCAL and Cursor Cloud, both started manually. Cursor Cloud is not auto-orchestration. `AUTOEXECUTION=OFF`.

## Evidence

Record task/packet hash, base revision, mode/risk, resolved authority, changed files, commands/outcomes, verification and reviewer evidence. Never attach real credentials, statements or private provider payloads.
