# Cursor Phase 0 Execution Prompt

Use this prompt only after the repository contains or is provided with this baseline package. Run in **Plan mode first**. Do not implement production features.

## ROLE
You are performing the repository architecture/governance bootstrap for `financial-platform`.

## OBJECTIVE
Install and reconcile the approved Architecture Baseline v1.0 into the repository so future agents have deterministic instructions before feature development starts.

## READ FIRST
- `AGENTS.md`
- `docs/planning/decision-register.md`
- `docs/planning/master-plan.md`
- all `docs/adr/ADR-*.md`
- `docs/architecture/architecture-baseline.md`
- `docs/architecture/websocket-architecture.md`
- `docs/connectors/connector-spi.md`
- `docs/domain/portfolio-xray.md`
- `docs/workflows/workflow-architecture.md`
- `docs/security/security-architecture.md`
- `docs/ux/dashboard-system.md`

## SCOPE
Documentation/governance/repository-skeleton only. You may create directories, indexes, scoped `AGENTS.md`, Cursor rules, ignore/protection files and validation scripts necessary for Phase 0.

## FORBIDDEN
- Do not bootstrap Spring/Angular production code yet.
- Do not connect to real financial providers.
- Do not add secrets or real financial fixtures.
- Do not change accepted ADR conclusions.
- Do not add infrastructure dependencies beyond the accepted baseline.
- Do not create REST business endpoints, SSE or polling mechanisms.

## PLAN REQUIREMENTS
Before editing, produce a file-by-file plan that:
1. inventories the current repository;
2. identifies conflicts/duplicates against the baseline;
3. lists exact files to add/update/delete;
4. states validation to perform;
5. flags any decision conflict instead of resolving it silently.

## IMPLEMENTATION REQUIREMENTS
After plan approval:
- preserve accepted decision wording/meaning;
- add documentation indexes/links so artefacts are discoverable;
- create concise scoped Cursor rules rather than duplicating whole ADRs into every prompt;
- add safe ignore patterns for secrets/runtime financial data;
- add a Phase 0 validation script/checklist where useful;
- ensure Mermaid/Markdown references resolve relative to repository structure.

## VALIDATION
At minimum:
- list final repository tree for governance/docs;
- search for contradictory mentions of SSE, application polling, microservices-first, direct LLM->MCP, automated trading, real financial fixtures or browser JWT storage;
- verify every Decision Register ADR reference exists;
- verify every ADR status is ACCEPTED;
- verify no protected secret/data files were introduced.

## DELIVERABLE
Return:
- summary;
- files created/changed;
- validation performed and results;
- detected pre-existing conflicts;
- any unresolved blocker;
- recommended single commit message in Spanish, gitflow-style, without mentioning Cursor/AI.

FIN
