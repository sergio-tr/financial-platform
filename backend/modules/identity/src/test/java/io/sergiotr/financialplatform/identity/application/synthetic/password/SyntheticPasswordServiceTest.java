package io.sergiotr.financialplatform.identity.application.synthetic.password;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idSyntheticPasswordHasher;
import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordPolicyViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyntheticPasswordServiceTest {

    private SyntheticPasswordService service;

    @BeforeEach
    void setUp() {
        service = new SyntheticPasswordService(SyntheticCommonPasswordBlocklist.defaultPolicy(), new Argon2idSyntheticPasswordHasher());
    }

    @Test
    void newPasswordRequiresPolicyButVerifyDoesNot() {
        var rejected = service.hashNewPassword("short");
        assertFalse(rejected.permitted());
        assertTrue(
                rejected.violationReason().filter(v -> v == SyntheticPasswordPolicyViolation.TOO_SHORT).isPresent());

        String legacy = "legacy";
        var legacyHash = new Argon2idSyntheticPasswordHasher().encode(legacy);
        assertTrue(service.verifyExistingPassword(legacy, legacyHash));

        String acceptable = "unique-passphrase!";
        var hashed = service.hashNewPassword(acceptable);
        assertTrue(hashed.permitted());
        assertNotNull(hashed.encodedHash());
        assertTrue(service.verifyExistingPassword(acceptable, hashed.encodedHash()));
        assertFalse(service.verifyExistingPassword("wrong", hashed.encodedHash()));
    }

    @Test
    void blocklistAppliesOnlyOnHashNew() {
        assertFalse(service.hashNewPassword("passwordpassword!").permitted());
    }
}