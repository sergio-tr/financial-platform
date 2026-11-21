package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaEvidencePort;
import java.util.Map;
import java.util.Objects;

/**
 * @deprecated Primary auth uses {@link io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idTrustedPrimaryAuthEvidence}.
 *             Retained name for fixture MFA maps in tests.
 */
@Deprecated
public final class SyntheticTrustedAuthEvidence implements TrustedMfaEvidencePort {

    private final SyntheticFixtureMfaEvidence delegate;

    public SyntheticTrustedAuthEvidence(Map<UserId, String> primaryRefs, Map<UserId, String> mfaRefs) {
        Objects.requireNonNull(primaryRefs, "primaryRefs");
        this.delegate = new SyntheticFixtureMfaEvidence(mfaRefs);
    }

    @Override
    public boolean acceptsMfa(UserId userId, String evidenceRef) {
        return delegate.acceptsMfa(userId, evidenceRef);
    }
}