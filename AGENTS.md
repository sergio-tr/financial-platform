# Financial Platform — Agent Instructions

## Mission
Build a local-first, multi-user personal financial analysis platform. The application runs on the user's own computer, is accessible only from the local network, uses Angular for the responsive web client and Spring Boot for the backend, and integrates financial/data providers through replaceable capability-based connectors. AI runs locally by default through Ollama and may analyze, explain, research and simulate but may never move money.

## Non-negotiable architecture

- Modular monolith with Spring Modulith.
- Java 25 LTS; Spring Boot 4.1.x; Angular 22.x baseline.
- Business traffic uses native secure WebSocket with STOMP 1.2. No SSE and no application-level polling.
- HTTPS exists only for static bootstrap, authentication/session bootstrap, WebSocket upgrade and health endpoints.
- AsyncAPI 3.1 + JSON Schema is the canonical frontend/backend message contract.
- Workspace is the tenancy boundary.
- PostgreSQL is the primary datastore; pgvector is used for local RAG embeddings.
- Temporal is self-hosted and hidden behind application ports.
- MCP types may exist only in infrastructure adapters. Domain/application modules cannot import MCP APIs.
- Connectors use segregated capability interfaces; there is no universal god interface.
- Portfolio X-Ray is a core bounded context and must support look-through, overlap, coverage and freshness.
- Financial calculations are deterministic code. LLMs interpret structured outputs; they do not calculate authoritative portfolio metrics.
- Financial execution tools (buy/sell/transfer/withdraw) are forbidden.
- Real credentials, portfolio exports, statements and production financial snapshots never enter Git, Cursor Cloud, Slack, fixtures or logs.

## Decision workflow

For any change affecting architecture, security, public contracts, data model, connector SPI, workflow semantics, financial algorithms or external dependencies:

1. Diagnose impact.
2. Read relevant ADRs and specifications.
3. Produce or update a plan.
4. If an accepted decision would change, create a superseding ADR before implementation.
5. Implement only after the decision is explicit.
6. Run required tests and architecture checks.
7. Report changed files, validation commands, deviations and remaining risks.

Never silently resolve an architectural ambiguity.

## Mandatory stop conditions

Stop implementation and report the decision needed when encountering:

- contradictory accepted ADRs;
- an unspecified security-sensitive behavior;
- a financial calculation without an agreed formula/precision policy;
- a potentially destructive migration without a migration strategy;
- a new infrastructure dependency not covered by an ADR;
- a connector that requires bypassing the capability SPI;
- a requirement to expose data outside the LAN or to an external SaaS without explicit data-sharing policy;
- a requirement to execute a real financial transaction.

## Code quality

- English identifiers.
- Minimal comments; comments explain intent or non-obvious constraints, not syntax.
- Domain modules do not depend on Spring, JPA, Temporal, MCP or transport types.
- Use value objects for money, rates, quantities, percentages and IDs.
- Never use `double`/`float` for authoritative monetary calculations.
- Avoid cross-bounded-context JPA relationships.
- Public message contracts are defined before handlers.
- New connectors must pass the connector contract test kit.

## Git governance

- `main` is protected.
- Branch prefixes: `feature/`, `bugfix/`, `refactor/`, `docs/`, `chore/`.
- Architecture: Issue -> ADR/spec -> approval -> implementation -> validation -> PR.
- Do not push directly to `main`.
- Do not force-push shared branches.
- Commit messages must describe the change, not the AI/tool that produced it.
