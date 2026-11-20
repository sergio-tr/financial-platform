package io.sergiotr.financialplatform.identity.domain;

public record AuthorizationDecision(boolean permitted, AuthorizationDenialReason denialReason) {

    public static AuthorizationDecision permit() {
        return new AuthorizationDecision(true, null);
    }

    public static AuthorizationDecision deny(AuthorizationDenialReason reason) {
        return new AuthorizationDecision(false, reason);
    }
}