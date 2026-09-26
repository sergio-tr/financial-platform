# Runbook: Data Export (R47)

Policy:
- Respect data classification (R46): SECRET is never exported; SECURITY_SENSITIVE excluded by default.
- Exports include provenance and applicable retention windows.

Steps:
1. Verify requester authorization and workspace policy.
2. Assemble export excluding SECRET and by-default SECURITY_SENSITIVE.
3. Provide format checksums and a clear data dictionary.

