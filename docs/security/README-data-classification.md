# Data Classification and Sink Policy (R46)

Status: CANONICAL (structure/policy). Inventory remains PENDING and owned by SER-109 and future MWUs.

Taxonomy:
- PUBLIC
- INTERNAL
- PERSONAL
- FINANCIAL_SENSITIVE
- SECURITY_SENSITIVE
- SECRET

Trust/provenance is a separate axis tracked by evidence and source policy.

Sink policy: deny-by-default. SECRET data only flows via dedicated secret-bearing transports + SecretVault/protected IPC with scoped leases. SECRET never traverses Rabbit/outbox/replay/Temporal/AI/RAG/telemetry.

Real PERSONAL/FINANCIAL/SECURITY/SECRET data must never enter Git/GitHub PR/issues, cloud CI/agents, Slack planning, Linear or Figma.

