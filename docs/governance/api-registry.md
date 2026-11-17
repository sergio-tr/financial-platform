# Public API Registry (R35)

Status: **INDEXED** — public Java API metadata is synchronized from accepted Linear contract packs (SER-92..SER-95, `ACCEPTED_FOR_P0_FREEZE` 2026-10-09 per issue comments and `P0_CONTRACTS_REREVIEW_REPORT_R3`). Compile-ready bodies and AsyncAPI/JSON Schema artifacts in Git remain **unmaterialized** (`contracts/**` owned by SER-70 / queue Q04).

## Canonical authority

| Issue | Linear contract pack (authority) | Git independent review (historical) |
|--------|----------------------------------|-------------------------------------|
| SER-92 | [Identity, Workspace, Settings and Audit v1](https://linear.app/sergiotr/document/ser-92-contract-pack-identity-workspace-settings-and-audit-v1-de3c80be0a59) (`de3c80be0a59`) | `docs/planning/phase0-closure/SER-92_INDEPENDENT_REVIEW.md` (2026-09-26, pre–freeze-closure) |
| SER-93 | [Connectivity, Instrument, Market, Macro and Research v1](https://linear.app/sergiotr/document/ser-93-contract-pack-connectivity-instrument-market-macro-and-research-faf775736687) (`faf775736687`) | `docs/planning/phase0-closure/SER-93_INDEPENDENT_REVIEW.md` |
| SER-94 | [Portfolio, X-Ray, Analytics and Risk v1](https://linear.app/sergiotr/document/ser-94-contract-pack-portfolio-x-ray-analytics-and-risk-7ca250b8fde1) (`7ca250b8fde1`) | `docs/planning/phase0-closure/SER-94_INDEPENDENT_REVIEW.md` |
| SER-95 | [Workflow, AI, Knowledge, Reporting and Notification v1](https://linear.app/sergiotr/document/ser-95-contract-pack-workflow-ai-knowledge-reporting-and-notification-ba08082c2706) (`ba08082c2706`) | `docs/planning/phase0-closure/SER-95_INDEPENDENT_REVIEW.md` |

Field-level records, outcome unions and use-case bodies are defined only in the Linear packs above. This registry does not duplicate them.

## Guidelines

- Only `<context>.api` is a public interface surface. Outbound ports are consumer-owned.
- Public contracts must not expose Spring/JPA/Temporal/MCP/provider SDK types.
- Transport DTOs differ from domain models and persistence entities.
- Java package roots declared in the packs use `io.sergiotr.financialplatform.<context>.api.*`.

## Registry

| Context | API owner (bounded context) | Versioning | Public interfaces / use cases (authority) | Contracts in Git | Callers | Examples / tests | Notes |
|--------|---------------------------|------------|-------------------------------------------|------------------|---------|------------------|-------|
| `identity.api` | `identity` | semver v1 | `AuthenticationApi`, `SessionApi`, `AuthenticatorApi`, `InvitationApi`, `UserQueryApi`, `SecurityAdminApi` | PENDING (`contracts/**`) | PENDING cross-context index | Linear SER-92 pack §11 | Host-local `identity.security-admin.host-recovery.begin:v1` → `HOST_LOCAL_CONTROL_ONLY` |
| `workspace.api` | `workspace` | semver v1 | `WorkspaceCommandApi`, `WorkspaceQueryApi`, `MembershipApi`, `WorkspaceAuthorizationApi` | PENDING | PENDING | Linear SER-92 pack §11 | `AUTHZ_REGISTRY_V1` tuples in pack freeze amendments |
| `configuration.api` | `configuration` | semver v1 | `PreferenceApi`, `ConfigurationQueryApi`, `ConfigurationCommandApi`, `FeatureFlagQueryApi`, `FeatureFlagCommandApi` | PENDING | PENDING | Linear SER-92 pack §11 | Settings/flags per P0-R38 |
| `audit.api` | `audit` | semver v1 | `AuditQueryApi`, `AuditExportApi` | PENDING | PENDING | Linear SER-92 pack §11 | No caller-created audit events |
| `connectivity.api` | `connectivity` | semver v1 | `ProviderCatalogQueryApi`, `ProviderAccountCommandApi`, `ProviderAccountQueryApi`, `ConnectorRegistryQueryApi`, `ProviderSynchronizationCommandApi`, `ProviderSynchronizationQueryApi` | PENDING | PENDING | Linear SER-93 pack §12 | Read-only provider connectors baseline |
| `instrument.api` | `instrument` | semver v1 | `InstrumentResolutionApi`, `InstrumentQueryApi`, `InstrumentAliasApi`, `CompositionQueryApi`, `ClassificationQueryApi`, `CorporateActionQueryApi`, `InstrumentCanonicalApplyApi` | PENDING | PENDING | Linear SER-93 pack §12 | `InstrumentCanonicalApplyApi` internal owner-apply |
| `market.api` | `market` | semver v1 | `MarketQuoteQueryApi`, `MarketSeriesQueryApi`, `FxQueryApi`, `BenchmarkQueryApi`, `MarketIngestionApi` | PENDING | PENDING | Linear SER-93 pack §12 | Decimal price/rate semantics in pack |
| `macro.api` | `macro` | semver v1 | `MacroSeriesQueryApi`, `MacroObservationQueryApi`, `MacroIngestionApi` | PENDING | PENDING | Linear SER-93 pack §12 | Vintage lineage in pack |
| `research.api` | `research` | semver v1 | `ResearchSourceQueryApi`, `NewsSearchApi`, `ResearchCaseCommandApi`, `ResearchCaseQueryApi`, `ResearchIngestionApi` | PENDING | PENDING | Linear SER-93 pack §12 | Untrusted external content classification |
| `portfolio.api` | `portfolio` | semver v1 | `PortfolioCommandApi`, `PortfolioQueryApi`, `TransactionQueryApi`, `PortfolioSnapshotQueryApi`, `PortfolioCanonicalApplyApi`, `PortfolioRecalculationApi` | PENDING | PENDING | Linear SER-94 pack §mandatory tests | Deterministic finance per P0-R24/R25 |
| `portfolio-xray.api` | `portfolio-xray` | semver v1 | `portfolio-xray.run.request:v1`, `portfolio-xray.run.cancel:v1`, `portfolio-xray.run.get:v1`, `portfolio-xray.result.get:v1`, `portfolio-xray.latest-compatible.get:v1`, `portfolio-xray.compare:v1` | PENDING | PENDING | Linear SER-94 pack §mandatory tests | UseCaseIds only; no separate `*Api` type names in pack headings |
| `analytics.api` | `analytics` | semver v1 | `PerformanceAnalysisApi`, `PortfolioAnalyticsApi`, `CorrelationAnalysisApi`, `AnalyticsQueryApi` | PENDING | PENDING | Linear SER-94 pack §mandatory tests | `CalculationArtifactView` ownership in pack |
| `risk.api` | `risk` | semver v1 | `RiskAnalysisApi`, `RiskResultQueryApi`, `StressScenarioApi` | PENDING | PENDING | Linear SER-94 pack §mandatory tests | |
| `workflow.api` | `workflow` | semver v1 | `WorkflowDefinitionQueryApi`, `WorkflowRunCommandApi`, `WorkflowRunQueryApi`, `WorkflowScheduleCommandApi`, `WorkflowScheduleQueryApi` | PENDING | PENDING | Linear SER-95 pack §22 | Temporal types infrastructure-only |
| `ai-orchestration.api` | `ai-orchestration` | semver v1 | `FinancialOrchestratorApi`, `AiRunQueryApi` | PENDING | PENDING | Linear SER-95 pack §22 | No model/MCP types in API |
| `knowledge.api` | `knowledge` | semver v1 | `KnowledgeCommandApi`, `KnowledgeQueryApi` | PENDING | PENDING | Linear SER-95 pack §22 | |
| `reporting.api` | `reporting` | semver v1 | `ReportingCommandApi`, `ReportingQueryApi` | PENDING | PENDING | Linear SER-95 pack §22 | |
| `notification.api` | `notification` | semver v1 | `NotificationCommandApi`, `NotificationDeliveryQueryApi` | PENDING | PENDING | Linear SER-95 pack §22 | |

`contracts/asyncapi/**` and `contracts/schemas/**` are intentionally absent until their owning materialization MWUs run. Until then, this registry is the Git index of accepted SER-92..SER-95 authority.