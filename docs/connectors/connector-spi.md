# Connector SPI and Capability Model

## Design objective
Provider integrations must be replaceable without modifying the canonical financial domain, analytics, Portfolio X-Ray, workflows or UI.

## Concept separation

- **Provider**: external institution/source, e.g. Trade Republic or MyInvestor.
- **ProviderAccount**: one workspace's configured logical account with a provider.
- **ConnectorImplementation**: a technical mechanism, e.g. MCP, file import, future official API.

A ProviderAccount can move from one connector implementation to another without changing its domain identity.

## Connector contract

Every connector exposes a descriptor and a set of capabilities. Do not create a universal interface with dozens of optional methods.

```java
public interface Connector {
    ConnectorDescriptor descriptor();
    Set<ConnectorCapabilityDescriptor> capabilities();
    ConnectorHealth health(ConnectorExecutionContext context);
}
```

## Initial capability catalogue

Financial account:
- `PortfolioSnapshotCapability`
- `TransactionHistoryCapability`
- `CashBalanceCapability`

Instrument/reference data:
- `InstrumentLookupCapability`
- `InstrumentMetadataCapability`
- `InstrumentCompositionCapability`
- `InstrumentClassificationCapability`

Market/macro/research:
- `MarketQuoteCapability`
- `HistoricalPriceCapability`
- `FxRateCapability`
- `MacroSeriesCapability`
- `NewsSearchCapability`
- `NewsContentCapability`
- `DocumentSearchCapability`
- `DocumentFetchCapability`

External communication:
- `NotificationCapability`
- `WorkspacePublishingCapability`
- `EmailReadCapability`
- `EmailSendCapability`

Capabilities are versioned semantically, e.g. `portfolio.snapshot:v1`.

## Result quality contract

Connector calls return data plus metadata:

```java
public record ConnectorResult<T>(
    T data,
    SourceMetadata source,
    Freshness freshness,
    Completeness completeness,
    List<ConnectorWarning> warnings
) {}
```

The platform must not treat partial or stale data as complete current truth.

## Execution context

Connector execution receives references, not plaintext credentials:
- workspace ID;
- provider account ID;
- trace ID;
- secret resolver reference;
- deadline;
- cancellation token/policy;
- execution policy.

## Exception taxonomy

- AuthenticationRequired
- AuthorizationFailed
- RateLimited
- Timeout
- ProviderUnavailable
- InvalidProviderResponse
- ConfigurationError
- UnsupportedCapability
- TransientConnectorError
- PermanentConnectorError

Application code handles typed connector failures rather than generic provider exceptions.

## MCP boundary

MCP client/server types may appear only inside infrastructure adapters. A connector implemented via MCP still implements the same capability interfaces as an HTTP/file/official-API connector.

## Synchronization

Provider accounts support `MANUAL`, `SCHEDULED`, and where supported `PUSH` synchronization policies. Scheduled pulls run as versioned workflows. Connector inbox/dedupe rules prevent duplicate source snapshots/transactions.

## Contract Test Kit

Every connector must pass reusable tests for:
- declared capability correctness;
- null/empty handling;
- timestamp semantics;
- currency/decimal precision;
- partial results;
- provenance/freshness/completeness;
- authentication errors;
- rate limits/timeouts;
- cancellation/deadline handling;
- deterministic mapping into canonical DTOs;
- secret non-leakage.
