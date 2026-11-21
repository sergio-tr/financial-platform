package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionDenialReason;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionResult;
import java.util.Objects;
import java.util.Optional;

/** Atomic outcome of a state-machine transition: denial or authoritative next session. */
public record SyntheticAuthTransition(
        AuthenticationTransitionResult result, Optional<SyntheticAuthSession> nextSession) {

    public SyntheticAuthTransition {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(nextSession, "nextSession");
        if (result.applied() && nextSession.isEmpty()) {
            throw new IllegalArgumentException("applied transition requires nextSession");
        }
        if (!result.applied() && nextSession.isPresent()) {
            throw new IllegalArgumentException("denied transition must not carry nextSession");
        }
    }

    public static SyntheticAuthTransition denied(AuthenticationTransitionDenialReason reason) {
        return new SyntheticAuthTransition(
                AuthenticationTransitionResult.denied(reason), Optional.empty());
    }

    public static SyntheticAuthTransition applied(
            AuthenticationTransitionResult result, SyntheticAuthSession session) {
        return new SyntheticAuthTransition(result, Optional.of(session));
    }
}