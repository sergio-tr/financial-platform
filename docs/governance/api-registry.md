# Public API Registry (R35)

Status: PLANNED — structure established; concrete records are PENDING until SER-92..SER-95.

Guidelines:
- Only `<context>.api` is a public interface. Outbound ports are consumer-owned.
- Public contracts must not expose Spring/JPA/Temporal/MCP/provider SDK types.
- Transport DTOs differ from domain models and persistence entities.

Registry (PENDING rows to be filled by owning MWUs):

| Context | API Owner | Versioning | Methods (UseCaseId) | Contracts (schema ids) | Callers | Examples/Tests | Notes |
|--------|-----------|------------|----------------------|-------------------------|---------|----------------|-------|
| identity.api | PENDING (SER-92) | semver | PENDING | PENDING | PENDING | PENDING | |
| workspace.api | PENDING (SER-92) | semver | PENDING | PENDING | PENDING | PENDING | |
| instrument.api | PENDING (SER-93) | semver | PENDING | PENDING | PENDING | PENDING | |
| portfolio.api | PENDING (SER-93) | semver | PENDING | PENDING | PENDING | PENDING | |
| portfolio-xray.api | PENDING (SER-94) | semver | PENDING | PENDING | PENDING | PENDING | |
| workflow.api | PENDING (SER-95) | semver | PENDING | PENDING | PENDING | PENDING | |

Contracts live under `contracts/asyncapi/**` and `contracts/schemas/**` once materialized by their MWUs. Until then, this registry holds PENDING placeholders only.

