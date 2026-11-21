package io.sergiotr.financialplatform.identity.domain;

import java.time.Instant;
import java.util.Objects;

/** In-memory synthetic session; not a persisted login or browser session. */
public record SyntheticAuthSession(
        UserId userId,
        AuthenticationStage stage,
        long authRevision,
        Instant stageExpiresAt,
        boolean disabled) {

    public SyntheticAuthSession {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(stageExpiresAt, "stageExpiresAt");
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
    }

    public TrustedWorkspaceActor toTrustedActor() {
        return new TrustedWorkspaceActor(userId, authRevision, stage.toAssuranceStatus());
    }
}