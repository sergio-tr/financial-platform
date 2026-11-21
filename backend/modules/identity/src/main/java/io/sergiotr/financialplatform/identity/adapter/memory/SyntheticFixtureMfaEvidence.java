package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaEvidencePort;
import java.util.Map;
import java.util.Objects;

/** Fixture-backed MFA evidence verifier for DEV/TEST only. */
public final class SyntheticFixtureMfaEvidence implements TrustedMfaEvidencePort {

    private final Map<UserId, String> mfaRefs;

    public SyntheticFixtureMfaEvidence(Map<UserId, String> mfaRefs) {
        this.mfaRefs = Map.copyOf(Objects.requireNonNull(mfaRefs, "mfaRefs"));
    }

    @Override
    public boolean acceptsMfa(UserId userId, String evidenceRef) {
        return evidenceRef != null && evidenceRef.equals(mfaRefs.get(userId));
    }
}