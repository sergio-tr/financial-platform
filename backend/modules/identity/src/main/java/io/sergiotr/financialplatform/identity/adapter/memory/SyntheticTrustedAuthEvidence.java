package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaEvidencePort;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import java.util.Map;
import java.util.Objects;

/** Fixture-backed evidence verifier for DEV/TEST; not production identity proof. */
public final class SyntheticTrustedAuthEvidence implements TrustedPrimaryAuthEvidencePort, TrustedMfaEvidencePort {

    private final Map<UserId, String> primaryRefs;
    private final Map<UserId, String> mfaRefs;

    public SyntheticTrustedAuthEvidence(Map<UserId, String> primaryRefs, Map<UserId, String> mfaRefs) {
        this.primaryRefs = Map.copyOf(Objects.requireNonNull(primaryRefs, "primaryRefs"));
        this.mfaRefs = Map.copyOf(Objects.requireNonNull(mfaRefs, "mfaRefs"));
    }

    @Override
    public boolean acceptsPrimaryAuth(UserId userId, String evidenceRef) {
        return evidenceRef != null && evidenceRef.equals(primaryRefs.get(userId));
    }

    @Override
    public boolean acceptsMfa(UserId userId, String evidenceRef) {
        return evidenceRef != null && evidenceRef.equals(mfaRefs.get(userId));
    }
}