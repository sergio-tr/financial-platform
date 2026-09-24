# Master Implementation Plan

## Objective
Deliver the platform incrementally while preserving replaceability of providers, deterministic financial calculations, LAN-only security, first-class Portfolio X-Ray and a stable agent-development process.

## Delivery principles

- Every phase has an explicit Definition of Done.
- Architecture and contracts precede implementation.
- New capabilities are introduced vertically, with domain + persistence + transport + tests, rather than by building disconnected horizontal layers.
- Synthetic data is used until provider connectors are hardened.
- Provider integrations are never prerequisites for validating core portfolio/X-Ray/analytics logic.
- Each phase should be mergeable independently.

## Phase 0 — Governance and Architecture Bootstrap

Deliver:
- Decision Register and accepted ADRs.
- Root and nested agent instructions.
- Cursor project rules, safe hooks and operating policy.
- C4 system/context/container/module views.
- WebSocket application protocol specification.
- Connector SPI and capability catalogue.
- Portfolio/X-Ray domain specification.
- Temporal workflow boundary.
- Security/threat model.
- UX/dashboard information architecture.
- Repository skeleton and documentation indexes.

DoD:
- no unresolved architecture decision blocks Phase 1;
- governance hierarchy is explicit;
- agent stop conditions are explicit;
- all security invariants are documented;
- no production feature code is required.

## Phase 1 — Runtime Skeleton and LAN Deployment

Deliver:
- monorepo skeleton;
- Java/Spring backend bootstrap;
- Angular bootstrap;
- PostgreSQL + Liquibase;
- Caddy LAN HTTPS/WSS;
- Temporal self-hosted bootstrap;
- Ollama connectivity adapter stub;
- health/readiness checks;
- local Compose environment;
- architecture test scaffolding.

DoD:
- app reachable from desktop/tablet/mobile on LAN via HTTPS;
- backend/database/Temporal/Ollama are not directly exposed to LAN;
- automated smoke test proves startup and WSS handshake.

## Phase 2 — Identity, Workspace and Session Security

Deliver:
- users, password hashing, server-side sessions;
- workspace + membership + roles;
- WebSocket principal propagation and authorization;
- audit baseline;
- secret-vault abstraction and local encrypted implementation.

DoD:
- cross-workspace access tests fail closed;
- sensitive logs are sanitized;
- WebSocket destinations enforce user/workspace authorization.

## Phase 3 — WebSocket Contract Platform

Deliver:
- AsyncAPI baseline;
- canonical envelopes;
- command/query/response/event/error semantics;
- correlation, trace and sequence IDs;
- reconnect/replay protocol;
- binary streaming protocol;
- Angular `RealtimeClient` and backend message gateway.

DoD:
- no feature creates its own socket;
- contract tests cover schema compatibility and error envelopes;
- reconnect/replay test passes without polling.

## Phase 4 — Canonical Financial Domain + Manual/Synthetic Import

Deliver:
- instruments, accounts, portfolios, positions, cash, transactions, snapshots;
- value objects and precision policy;
- manual/synthetic import connector;
- immutable snapshot persistence;
- canonical identifiers/provenance.

DoD:
- core domain works without Trade Republic/MyInvestor;
- TWR/MWR prerequisites are represented correctly;
- provider-specific fields do not leak into portfolio domain.

## Phase 5 — Connector Platform

Deliver:
- connector registry;
- descriptor/configuration/auth metadata;
- capability negotiation;
- result quality metadata;
- connector exception taxonomy;
- contract test kit;
- synchronization policy and connector inbox/dedupe.

DoD:
- two synthetic connector implementations prove replaceability;
- unsupported capabilities fail explicitly;
- connector contract tests are reusable.

## Phase 6 — Portfolio X-Ray Core

Deliver:
- composition model;
- recursive look-through with cycle detection and max depth;
- asset-class, sector, country, currency, issuer, index and underlying exposures;
- weighted overlap;
- HHI/top-N/effective holdings;
- data coverage/freshness;
- deterministic X-Ray service and persistence.

DoD:
- golden datasets cover nested funds, partial composition and cycles;
- unknown weight is preserved as unknown;
- authoritative numbers come exclusively from deterministic calculations.

## Phase 7 — Analytics and Risk Baseline

Deliver:
- performance, return, volatility, drawdown, allocation, FX exposure, concentration and diversification calculations;
- benchmark interfaces;
- property-based and golden tests;
- mutation testing for critical math.

## Phase 8 — Investment Dashboard UX

Deliver:
- custom design tokens/components;
- responsive mobile/tablet/desktop layouts;
- portfolio summary/performance/positions;
- X-Ray overview and dedicated X-Ray page;
- ECharts/Lightweight Charts integration;
- accessibility and visual regression baseline;
- configurable widget registry.

## Phase 9 — Temporal Workflow Foundation

Deliver:
- `WorkflowEnginePort`;
- Temporal adapter;
- workflow versioning;
- durable status/events/cancellation/timeouts/retries;
- manual and scheduled provider-sync workflow;
- WebSocket workflow events through outbox/realtime publisher.

## Phase 10 — External Data: Market/Macro/News

Deliver replaceable connectors for:
- market quotes and history;
- FX;
- macro series;
- news/search/content.

No single vendor becomes a domain dependency.

## Phase 11 — Trade Republic Read-Only Connector

Deliver only approved read capabilities. Treat unofficial/reverse-engineered integration as replaceable and potentially fragile. No trading endpoints are exposed to tools/workflows.

## Phase 12 — MyInvestor Connectors

Separate public/catalogue capabilities from personal-account capabilities. Support file/manual fallback so product features never depend on an unofficial personal-account API.

## Phase 13 — AI Orchestrator

Deliver:
- model gateway + Ollama adapter;
- skill registry;
- tool registry;
- context builder/budget;
- policy engine;
- evidence/result validation;
- streaming over WebSocket;
- structured outputs.

## Phase 14 — RAG and Research

Deliver document ingestion/versioning/chunking/embeddings, pgvector retrieval, evidence provenance and research workflows.

## Phase 15 — Financial Analysis Workflows

Implement incrementally:
- portfolio-health;
- portfolio-xray analysis;
- asset/fund/crypto deep dive;
- macro outlook;
- news impact;
- geopolitical risk;
- stress tests;
- rebalance simulation;
- morning/weekly/monthly reports.

## Phase 16 — Slack / Notion / Email Integrations

Default data policy remains `LOCAL_ONLY`. External publishing requires explicit workspace policy and sanitized payloads.

## Phase 17 — Hardening and Operations

Deliver backups + restore tests, observability dashboards, failure drills, dependency/security scans, rate limiting, connector resilience, performance and long-running workflow tests.

## Phase 18 — Advanced Analysis

Potential later capabilities: historical replay, macro-regime classification, decision journal, portfolio drift, scenario library, research notebook, counter-thesis analysis and richer evidence confidence/coverage indicators.
