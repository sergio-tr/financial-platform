# Field-level privacy catalog (SER-109)

Phase 0 machine-readable **field** classification, permitted/denied sink vocabulary, and retention cross-refs.

- Schema: `docs/security/field-privacy-catalog.schema.json`
- SER-160 slice: `identity-workspace-session-mfa.v1.yaml`
- SER-161 slice: `manual-import-portfolio-snapshot.v1.yaml` (manual import / portfolio / snapshots)
- Regression: `scripts/governance/validate-field-privacy-catalogs-regression.py` (both slices)
- Slice validators: `validate-ser-160-field-privacy-catalog.py`, `validate-ser-161-field-privacy-catalog.py`

Class-level deny-by-default policy remains in `docs/security/data-classification.yaml` (global class owners still `PENDING (SER-109)`). This slice assigns **field** owners only.

Static CI validation; does not enforce runtime egress.