# Runbook: Provider Disconnect (R47)

Objective: revoke provider access and stop sync while preserving imported history unless a separate delete is requested.

Steps:
1. Revoke/rotate provider credentials in SecretVault.
2. Stop scheduled/temporal sync workflows for the provider account.
3. Confirm no further pulls occur; audit event recorded.

