# Workflow Architecture

## Runtime
Temporal self-hosted is the initial durable workflow runtime. Temporal is infrastructure; financial/application modules depend on `WorkflowEnginePort`, not Temporal SDK annotations.

## Application port

```java
public interface WorkflowEnginePort {
    WorkflowRunId start(WorkflowDefinitionId definition, WorkflowInput input);
    void cancel(WorkflowRunId runId);
    void signal(WorkflowRunId runId, WorkflowSignal signal);
    WorkflowRunStatus getStatus(WorkflowRunId runId);
}
```

## Temporal adapter rules

- `@WorkflowInterface`, Temporal Activities and Temporal client types stay under infrastructure workflow packages.
- Workflow histories carry IDs/references, not credentials, raw documents, giant portfolio payloads or secret context.
- Activities resolve required current data through application ports.
- Workflow code must respect Temporal determinism constraints.

## Versioning

Every workflow definition has an explicit code and version, e.g. `portfolio-health:v1`. Historical runs remain associated with the version actually executed.

## Initial workflow catalogue

Foundation:
- `provider-sync`
- `portfolio-xray`
- `portfolio-health`

Later:
- asset/fund/crypto deep dive;
- macro outlook;
- news impact;
- geopolitical risk;
- stress test;
- rebalance simulation;
- morning brief;
- weekly digest;
- monthly report;
- research report.

## Communication with UI

Temporal never publishes directly to Angular. Workflow progress/result changes become application events, are persisted durably through the transactional outbox, then published by the realtime layer over WebSocket.

## Retry and idempotency

- retries are configured per Activity based on transient/permanent failure taxonomy;
- external calls use idempotency/source identifiers where available;
- write Activities must be safe under retry;
- scheduled sync deduplicates provider snapshots and transactions.

## Cancellation/timeouts

Every long-running workflow must define cancellation behavior and meaningful Activity/workflow timeouts. Cancellation never leaves partially written financial state without a recoverable status.
