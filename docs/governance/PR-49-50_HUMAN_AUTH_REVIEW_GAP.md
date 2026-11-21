# Auth merge review gap (PR #49, PR #50, PR #52)

**Status:** OPEN — blocks real login, production activation, or release claims.

GitHub records Cursor Approval Agent comments with explicit **No apruebo** on PR #49 and PR #50 while both were squash-merged. PR #52 (SER-152 TOTP core) had **zero** GitHub reviews at merge time. Bugbot was **SKIPPED** (usage limit) where applicable; CI green does **not** constitute independent human security approval.

**Required before production login:**

1. Human reviewer with security mandate re-reads synthetic auth hardening (SER-149), password/Argon2id changes (SER-150, SER-151), and TOTP trust boundary work (SER-152, SER-153).
2. Record APPROVED review on a follow-up PR or documented remediation ticket if policy requires retroactive sign-off.

**Invariants:** `IMPLEMENTATION_LOCK=ON`, `P0_IMPLEMENTATION_READY=NO`, `DEV_SYNTHETIC_V1_GATE=SCOPED` unchanged.