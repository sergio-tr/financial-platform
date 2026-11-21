package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;

/**
 * Server-side MFA evidence check. In-memory synthetic only; does not implement PostgreSQL TOTP CAS (Iteration 20).
 */
public interface TrustedMfaEvidencePort {

    boolean acceptsMfa(UserId userId, String evidenceRef);
}