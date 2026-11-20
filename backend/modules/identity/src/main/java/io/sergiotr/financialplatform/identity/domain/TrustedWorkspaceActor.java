package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;

/** Server-side actor context: never accepted from caller-supplied transport parameters in the trusted boundary. */
public record TrustedWorkspaceActor(UserId userId, long authRevision, SecurityAssuranceStatus assurance) {

    public TrustedWorkspaceActor {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(assurance, "assurance");
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
    }

    public boolean hasWorkspaceAssurance() {
        return assurance == SecurityAssuranceStatus.AUTHENTICATED_FULL;
    }
}