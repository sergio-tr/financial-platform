# Decision Register

Canonical status vocabulary: `LOCKED`, `DEFERRED`, `SUPERSEDED`.

`LOCKED` means an implementation agent may not reinterpret the decision. A change requires a superseding ADR.

| ID | Decision | Status | ADR |
|---|---|---:|---|
| D-001 | Modular monolith with Spring Modulith | LOCKED | ADR-0001 |
| D-002 | Java 25 LTS + Spring Boot 4.1.x + Angular 22.x baseline | LOCKED | ADR-0002 |
| D-003 | STOMP 1.2 over native WebSocket/WSS | LOCKED | ADR-0003 |
| D-004 | Business traffic over WSS; HTTPS only for bootstrap/auth/upgrade/health | LOCKED | ADR-0004 |
| D-005 | AsyncAPI 3.1 + JSON Schema contract-first | LOCKED | ADR-0005 |
| D-006 | Workspace is the tenancy boundary | LOCKED | ADR-0006 |
| D-007 | Scheduled provider pulls are allowed and modeled as workflows | LOCKED | ADR-0007 |
| D-008 | Temporal self-hosted is the workflow runtime, behind a port | LOCKED | ADR-0008 |
| D-009 | MCP is infrastructure-only | LOCKED | ADR-0009 |
| D-010 | Connector SPI is capability-based and segregated | LOCKED | ADR-0010 |
| D-011 | Portfolio X-Ray is a core bounded context | LOCKED | ADR-0011 |
| D-012 | AI architecture uses an orchestrator + skills + tools + policies | LOCKED | ADR-0012 |
| D-013 | Custom investment-oriented design system | LOCKED | ADR-0013 |
| D-014 | ECharts + Lightweight Charts are the initial charting stack | LOCKED | ADR-0014 |
| D-015 | No real financial execution by AI or automated workflow | LOCKED | ADR-0015 |
| D-016 | No application-level polling; reconnect/heartbeat/internal Temporal pollers are infrastructure concerns | LOCKED | ADR-0016 |
| D-017 | LAN-only deployment; no inbound WAN exposure | LOCKED | ADR-0017 |
| D-018 | PostgreSQL + pgvector is primary persistence/RAG storage | LOCKED | ADR-0018 |
| D-019 | Secrets are resolved through `SecretVaultPort`; encrypted values never enter LLM context | LOCKED | ADR-0019 |
| D-020 | Financial values use explicit value objects + BigDecimal | LOCKED | ADR-0020 |
| D-021 | Transactional outbox for durable application events | LOCKED | ADR-0021 |
| D-022 | External data-sharing is policy-controlled and defaults to LOCAL_ONLY | LOCKED | ADR-0022 |
| D-023 | Cursor/agent work is governed by repository rules and cost-aware escalation | LOCKED | ADR-0023 |
| D-024 | Spring MVC + WebSocket + virtual threads; no reactive domain architecture | LOCKED | ADR-0024 |
| D-025 | Internal IDs use UUIDv7; provider identifiers remain opaque external IDs | LOCKED | ADR-0025 |
| D-026 | Events use UTC `Instant`; business dates use `LocalDate`; workspace owns `ZoneId` | LOCKED | ADR-0026 |
| D-027 | Portfolio snapshots are immutable append-only historical snapshots | LOCKED | ADR-0027 |
| D-028 | Authentication uses server-side session + Secure/HttpOnly/SameSite cookies; no browser-stored JWT | LOCKED | ADR-0028 |
| D-029 | PWA shell may cache; sensitive financial data is not persisted for offline use | LOCKED | ADR-0029 |
| D-030 | No Kafka, RabbitMQ, Redis or Kubernetes in the initial platform | LOCKED | ADR-0030 |
| D-031 | Dashboard layouts are responsive and independently configurable for mobile/tablet/desktop | LOCKED | ADR-0031 |
| D-032 | No global NgRx by default; feature facades/stores use Signals + RxJS | LOCKED | ADR-0032 |
| D-033 | Real financial data is forbidden in source control, cloud-agent context and test fixtures | LOCKED | ADR-0033 |
| D-034 | Connector results carry provenance, freshness, completeness and warnings | LOCKED | ADR-0034 |
| D-035 | X-Ray never redistributes unknown composition weight as if known | LOCKED | ADR-0035 |
| D-036 | Exact local LLM model is a benchmarked runtime configuration, not an architecture constant | LOCKED | ADR-0036 |

## Configuration, not architecture

The following are intentionally **not** hard-coded architectural decisions: exact Ollama model; exact provider sync schedule; exact market-data vendor; exact MyInvestor/Trade Republic connector implementation; dashboard widget arrangement; workspace timezone default overrides; model profile mapping; notification destinations. These remain runtime/configuration choices behind defined ports.
