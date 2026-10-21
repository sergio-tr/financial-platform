# Synthetic Fixture Policy

Owner: SER-116 / P0-R55

Only synthetic deterministic data is allowed in repository fixtures and automated acceptance.

- Every normative fixture has a stable FIX-* ID.
- catalog.yaml records schema version, deterministic clock/seed and content/generator hash metadata.
- Personas, portfolios, instruments and provider responses are fictional.
- Financial golden expectations are reviewed independently of implementation output.
- Provider simulators cover success, rate limit, auth failure, timeout, malformed, unsupported, transient and permanent failures.
- Real or merely redacted provider payloads/statements/accounts are forbidden.
- Dev/acceptance reset uses fixture IDs; production-local reset/seed commands are forbidden.
- Fixture changes are reviewed like contract changes when normative behavior changes.
