# Cursor Operating Model and Cost Governance

## Purpose
Use Cursor as an implementation executor without allowing agents to invent architecture or consume premium model credits indiscriminately.

## Modes

### Ask
Use for read-only diagnosis, locating code, explaining behavior and comparing implementation alternatives. No file changes.

### Plan
Mandatory before changes that affect multiple modules, architecture, security, database migrations, WebSocket contracts, connectors, workflows, financial algorithms, Portfolio X-Ray or AI tools. The plan must cite relevant repository documents and identify files/contracts/tests affected.

### Agent
Used to implement an approved, bounded plan. It must not reopen accepted architecture unless a stop condition is hit.

### Debug
Use only when runtime evidence is required to diagnose a non-obvious defect.

## Model escalation policy

Default implementation/repository work: a cost-efficient Cursor model profile. Premium reasoning models are reserved for architecture, security, difficult financial algorithms, hard cross-module failures and review after a cheaper attempt is inadequate.

Do not use fast/premium variants by default. Do not expand context to the whole repository "just in case".

## Context budget

Every task prompt must contain:
- objective;
- scope;
- documents/files to read first;
- allowed files/directories;
- forbidden files/directories;
- architectural constraints;
- acceptance criteria;
- validation commands;
- stop conditions;
- requested deliverable format.

Prefer several small issues over one mega-prompt.

## Agent authority

Agents may choose implementation details only when all of these are true:
1. the choice does not contradict an ADR;
2. it is local to the task;
3. it does not create a new external dependency;
4. it does not alter a public contract;
5. it does not affect financial correctness or security invariants.

Otherwise stop and request/record a decision.

## Required output after implementation

- summary of work;
- files changed;
- commands/tests executed and results;
- contract/migration changes;
- deviations from plan;
- unresolved risks;
- documentation/ADR impact.

## Credit-preservation rules

- Search narrowly before reading full files.
- Read only ADRs/specifications applicable to the task.
- Avoid duplicate independent agents unless results need genuinely independent verification.
- Use premium review only at architectural/security/math boundaries.
- Revert and refine a bad plan rather than repeatedly patching a drifting implementation.
- Do not ask multiple agents to solve the same routine task competitively.
- Keep fixtures small and synthetic.
