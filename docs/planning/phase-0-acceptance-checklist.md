# Phase 0 Acceptance Checklist

Planning/documentation authority after P0-R59 / SER-132:

```text
P0_PLANNING_FROZEN=YES
DOCUMENTATION_PLANNING_CLOSED=YES
ARCHITECTURE_DISCOVERY_MODE=OFF
EXECUTION_MODE=MANUAL_CURSOR
AUTOEXECUTION=OFF
IMPLEMENTATION_LOCK=ON
P0_IMPLEMENTATION_READY=NO
```

Remaining Phase-0 work is materialization/verification executed manually from Cursor in P0-R54 order — not architecture discovery.

Phase 0 implementation unlock (`P0_IMPLEMENTATION_READY`) requires later audits (including SER-98 / SER-20). This checklist does not flip that gate.

Repository governance artefacts expected on `main`:

- [x] Decision Register is committed.
- [x] All accepted ADRs are committed and indexed.
- [x] `AGENTS.md` and project rules are installed.
- [x] Security-sensitive paths are excluded from Git/agent context.
- [x] Architecture baseline and bounded-context map are reviewed.
- [x] WebSocket protocol and AsyncAPI ownership rules are reviewed. (R26/R42)
- [x] Connector SPI/capability rules are reviewed.
- [x] Portfolio X-Ray specification is reviewed.
- [x] Temporal workflow boundary is reviewed.
- [x] Security architecture/threat model is reviewed. (R41/R43/R46/R47)
- [x] Dashboard/UX system is reviewed.
- [x] Master implementation plan is approved.
- [x] Cursor operating model and stop conditions are approved (manual execution baseline P0-R59).
- [x] Repository skeleton has no production secrets or real portfolio data.
- [x] No application feature implementation has started ahead of these governance artefacts.
- [x] Traceability graph/schema exists and validates. (R37/R39)
- [x] Requirements catalogs exist under `docs/requirements/functional/**` and `docs/requirements/nfr/**`. (R40)
- [x] Public API registry structure exists. (R35)
- [x] Verification catalog structure exists. (R44)
- [x] Design manifest structure and tokens directory exist. (R45)
- [x] Manual Cursor execution baseline is materialised (`docs/governance/execution-baseline.md`).
- [ ] V0 substrate materialization/verification tickets complete in P0-R54 order (manual selection).
- [ ] SER-98 post-Git traceability proof.
- [ ] SER-20 implementation-unlock audit (`P0_IMPLEMENTATION_READY` gate).
