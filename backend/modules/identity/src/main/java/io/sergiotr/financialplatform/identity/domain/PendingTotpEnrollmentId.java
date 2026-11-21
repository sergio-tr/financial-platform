package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Opaque server-issued pending TOTP enrollment handle (synthetic DEV/TEST). */
public record PendingTotpEnrollmentId(UUID value) {

    public PendingTotpEnrollmentId {
        Objects.requireNonNull(value, "value");
    }

    public static PendingTotpEnrollmentId generate() {
        return new PendingTotpEnrollmentId(UUID.randomUUID());
    }

    public static PendingTotpEnrollmentId of(UUID value) {
        return new PendingTotpEnrollmentId(value);
    }

    @Override
    public String toString() {
        return "PendingTotpEnrollmentId[" + value + "]";
    }
}