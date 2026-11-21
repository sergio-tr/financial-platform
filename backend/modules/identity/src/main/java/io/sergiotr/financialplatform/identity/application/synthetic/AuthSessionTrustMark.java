package io.sergiotr.financialplatform.identity.application.synthetic;

/** Package-private issuance mark; not forgeable from outside this package. */
final class AuthSessionTrustMark {

    enum Kind {
        BOOTSTRAP,
        INTERMEDIATE,
        AUTHENTICATED
    }

    private final Kind kind;

    private AuthSessionTrustMark(Kind kind) {
        this.kind = kind;
    }

    static AuthSessionTrustMark bootstrap() {
        return new AuthSessionTrustMark(Kind.BOOTSTRAP);
    }

    static AuthSessionTrustMark intermediate() {
        return new AuthSessionTrustMark(Kind.INTERMEDIATE);
    }

    static AuthSessionTrustMark authenticated() {
        return new AuthSessionTrustMark(Kind.AUTHENTICATED);
    }

    static boolean isAuthenticated(AuthSessionTrustMark mark) {
        return mark != null && mark.kind == Kind.AUTHENTICATED;
    }
}