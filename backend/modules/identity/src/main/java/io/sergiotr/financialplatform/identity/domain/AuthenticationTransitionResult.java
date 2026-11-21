package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.Optional;

public record AuthenticationTransitionResult(
        boolean applied, Optional<AuthenticationStage> stage, AuthenticationTransitionDenialReason denialReason) {

    public AuthenticationTransitionResult {
        Objects.requireNonNull(denialReason, "denialReason");
    }

    public static AuthenticationTransitionResult applied(AuthenticationStage stage) {
        return new AuthenticationTransitionResult(true, Optional.of(stage), AuthenticationTransitionDenialReason.NONE);
    }

    public static AuthenticationTransitionResult denied(AuthenticationTransitionDenialReason reason) {
        return new AuthenticationTransitionResult(false, Optional.empty(), reason);
    }
}