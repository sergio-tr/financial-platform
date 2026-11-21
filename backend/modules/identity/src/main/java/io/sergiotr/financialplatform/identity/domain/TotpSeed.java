package io.sergiotr.financialplatform.identity.domain;

import java.util.Arrays;
import java.util.Objects;

/** Opaque TOTP seed material; never log or expose in exceptions. */
public final class TotpSeed {

    private final byte[] material;

    public TotpSeed(byte[] material) {
        Objects.requireNonNull(material, "material");
        if (material.length == 0) {
            throw new IllegalArgumentException("empty seed");
        }
        this.material = material.clone();
    }

    public byte[] copyMaterial() {
        return material.clone();
    }

    public int length() {
        return material.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TotpSeed other)) {
            return false;
        }
        return Arrays.equals(material, other.material);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(material);
    }

    @Override
    public String toString() {
        return "TotpSeed[len=" + material.length + ']';
    }
}