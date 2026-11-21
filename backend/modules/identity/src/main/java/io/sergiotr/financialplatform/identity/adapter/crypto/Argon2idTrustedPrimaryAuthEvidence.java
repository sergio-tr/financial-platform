package io.sergiotr.financialplatform.identity.adapter.crypto;

import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordService;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.SyntheticPasswordCredentialReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import java.util.Objects;

/**
 * Verifies primary auth by matching caller-supplied password material against server-owned Argon2id hashes.
 * evidenceRef is raw password bytes as {@link String} (synthetic/tests only; never exposed via HTTP login).
 */
public final class Argon2idTrustedPrimaryAuthEvidence implements TrustedPrimaryAuthEvidencePort {

    private static final String TIMING_DUMMY_PASSWORD = "__synthetic-timing-dummy-password__";

    private final SyntheticPasswordCredentialReadPort credentials;
    private final SyntheticPasswordService passwordService;
    private final String timingDummyHash;

    public Argon2idTrustedPrimaryAuthEvidence(
            SyntheticPasswordCredentialReadPort credentials, SyntheticPasswordService passwordService) {
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.passwordService = Objects.requireNonNull(passwordService, "passwordService");
        this.timingDummyHash = new Argon2idSyntheticPasswordHasher().encode(TIMING_DUMMY_PASSWORD);
    }

    @Override
    public boolean acceptsPrimaryAuth(UserId userId, String evidenceRef) {
        if (userId == null || evidenceRef == null) {
            return false;
        }
        var stored = credentials.findByUserId(userId);
        if (stored.isEmpty() || stored.get().disabled()) {
            passwordService.verifyExistingPassword(evidenceRef, timingDummyHash);
            return false;
        }
        return passwordService.verifyExistingPassword(evidenceRef, stored.get().encodedHash());
    }
}