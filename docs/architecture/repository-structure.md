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
├── infrastructure/
├── docs/
│   ├── adr/
│   ├── architecture/
│   ├── connectors/
│   ├── domain/
│   ├── planning/
│   ├── security/
│   ├── ux/
│   └── workflows/
├── scripts/
├── .cursor/
│   └── rules/
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
- `.cursor/rules`: enforceable project-agent context; keep rules concise and scoped.

Nested `AGENTS.md` files may be added to `backend`, `frontend`, and `contracts` once those directories exist, to avoid loading irrelevant implementation guidance globally.
