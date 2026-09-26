# Repository Structure

Target monorepo layout:

```text
financial-platform/
├── backend/
│   ├── application/
│   └── modules/
├── frontend/
├── contracts/
│   ├── asyncapi/
│   └── schemas/
├── design/
│   └── tokens/
├── infrastructure/
├── docs/
│   ├── adr/
│   ├── architecture/
│   ├── connectors/
│   ├── domain/
│   ├── planning/
│   ├── governance/
│   ├── requirements/
│   │   ├── functional/
│   │   └── nfr/
│   ├── verification/
│   ├── design/
│   ├── security/
│   ├── ux/
│   └── workflows/
├── scripts/
├── .cursor/
│   └── rules/
├── .github/
├── AGENTS.md
├── compose.yaml
└── README.md
```

## Ownership rules

- `contracts/`: transport schemas only, provider-neutral.
- `backend/modules/<context>`: context-owned domain/application/infrastructure slices according to module conventions.
- `infrastructure/`: host/deployment definitions, not business logic.
- `docs/adr`: architectural decisions only.
- `docs/planning`: phase/issue/agent execution governance.
- `docs/governance`: architecture/traceability manifests and adjudication.
- `docs/requirements`: functional/NFR requirement records (Phase 0 = LOCKED).
- `docs/verification`: verification evidence catalog structure (R44).
- `docs/design`: design manifest (R45); `design/tokens` are implementation-authoritative tokens.
- `docs/contracts`: guidance and index for materialized transport contracts (R35).
- `.cursor/rules`: enforceable project-agent context; keep rules concise and scoped.
- `.github`: PR templates and contribution governance.

Nested `AGENTS.md` files may be added to `backend`, `frontend`, and `contracts` once those directories exist, to avoid loading irrelevant implementation guidance globally.
