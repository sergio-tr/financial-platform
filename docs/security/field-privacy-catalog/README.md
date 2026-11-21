# Field-level privacy catalog (SER-109 / SER-160)

Phase 0 machine-readable **field** classification, permitted/denied sink vocabulary, and retention cross-refs for Identity, Workspace, Session, and MFA families.

- Schema: `docs/security/field-privacy-catalog.schema.json`
- Slice v1: `identity-workspace-session-mfa.v1.yaml`
- Validator: `scripts/governance/validate-ser-160-field-privacy-catalog.py`

Class-level deny-by-default policy remains in `docs/security/data-classification.yaml` (global class owners still `PENDING (SER-109)`). This slice assigns **field** owners only.

Static CI validation; does not enforce runtime egress.