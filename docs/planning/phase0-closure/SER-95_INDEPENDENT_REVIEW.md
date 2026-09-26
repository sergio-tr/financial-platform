# SER-95 Independent READ-ONLY review — 2026-09-26

Input: SER-95 Contract Pack v1 + orchestration amendment v1.1 (ba08082c2706).
Result: **CHANGES_REQUIRED**

## Finding IDs

### SER95-IR-001 — Workflow input schema field sets not defined
Severity: BLOCKING
Amendment binds workflow codes to schema IDs (e.g. `workflow.provider-sync.input:v1`) but does not define the fields of those schemas. Compile-ready commands/DTO mapping still requires invention of input fields.

### SER95-IR-002 — AiResultView sections / structured schema-owned fields undefined
Severity: BLOCKING
`sections/structured schema-owned fields` and ClaimView statement constraints lack exact schema IDs/field records for ANALYSIS|OBSERVATION|SCENARIO|SIGNAL|RECOMMENDATION result types.

### SER95-IR-003 — ToolDefinition registry not enumerated
Severity: BLOCKING for AI boundary executability
Tool effect classes exist, but no accepted ToolDefinition IDs/versions/effectClass/egress matrix is listed. An agent could invent tool IDs.

### SER95-IR-004 — NotificationDefinition / ReportDefinition registries incomplete
Severity: BLOCKING
PublishNotificationCommand and RequestReportCommand depend on definition/template/schema refs without enumerating accepted definition IDs and their schemas beyond workflow list.

### SER95-IR-005 — Temporal leakage check PASS for declared types
Severity: INFO
No Temporal/MCP/Ollama/Spring AI types appear in public API declarations. ExternalEffect state machine and OUTCOME_UNKNOWN transition table are concrete and valuable.

## Verdict
`CHANGES_REQUIRED`
