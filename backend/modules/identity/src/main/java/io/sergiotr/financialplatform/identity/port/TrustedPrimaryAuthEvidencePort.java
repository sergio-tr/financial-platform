package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;

/** Server-side verifier for synthetic primary-auth evidence (test fixtures only in V1 slice). */
public interface TrustedPrimaryAuthEvidencePort {

    boolean acceptsPrimaryAuth(UserId userId, String evidenceRef);
}