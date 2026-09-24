# AI Orchestration Architecture

## Core structure

`FinancialOrchestrator` coordinates:
- intent classification;
- planner;
- skill registry;
- tool registry;
- context builder;
- context budget manager;
- evidence engine;
- policy engine;
- prompt registry/versioning;
- model gateway;
- structured-output/result validation.

## No direct MCP access

Required path:
LLM -> Tool definition -> Tool handler -> Application service -> Port -> Connector capability -> Infrastructure adapter -> MCP/API/file.

This guarantees workspace authorization, validation, audit, rate limits, redaction and data policy before an external tool is touched.

## Skill contract

Each skill defines:
- purpose and instructions;
- allowed tools;
- required context providers;
- context budget;
- output JSON schema;
- evidence requirements;
- forbidden operations.

Initial skill families: portfolio analysis, X-Ray analysis, risk, fund, crypto, market, macro, news, geopolitical, scenario, research, reporting.

## Financial correctness boundary

Deterministic Java services calculate authoritative values. AI receives structured calculated facts and may interpret them. Outputs must distinguish facts, calculations and model inference.

## Tool permissions

Permission classes:
- READ
- WRITE_SAFE
- WRITE_SENSITIVE
- FORBIDDEN

BUY, SELL, TRANSFER and WITHDRAW are always FORBIDDEN.

## Local models

`ChatModelPort` and `EmbeddingModelPort` abstract the runtime. Ollama is the initial adapter. Exact models are selected by hardware benchmark/profile and configuration rather than hard-coded architecture.

## Model profiles

Logical profiles may include FAST, BALANCED, DEEP_REASONING and EMBEDDING. A workspace/installation maps profiles to concrete local models.

## Evidence

Relevant claims carry evidence references classified as DATA, CALCULATION, DOCUMENT, NEWS, MACRO or MODEL_INFERENCE. AI output validation rejects/flags authoritative claims lacking required evidence.
