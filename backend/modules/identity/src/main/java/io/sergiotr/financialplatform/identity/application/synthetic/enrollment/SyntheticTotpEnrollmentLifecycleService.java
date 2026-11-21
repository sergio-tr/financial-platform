package io.sergiotr.financialplatform.identity.application.synthetic.enrollment;

import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.application.synthetic.totp.Rfc6238TotpCryptographicVerifier;
import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.PendingTotpEnrollmentId;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AccountSecuritySnapshotReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedMfaChallengeContextPort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeSnapshot;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Two-phase synthetic TOTP enrollment (SER-24 I16): trusted challenge after primary, 10m pending TTL,
 * ephemeral server-side seed, RFC6238 confirmation. Does not grant {@code AUTHENTICATED_FULL} or runtime ACTIVE.
 */
public final class SyntheticTotpEnrollmentLifecycleService {

    public static final Duration PENDING_TTL = Duration.ofMinutes(10);

    enum PendingStatus {
        PENDING,
        CONFIRMED_SYNTHETIC,
        CANCELLED,
        EXPIRED
    }

    private final Clock clock;
    private final AccountSecuritySnapshotReadPort securityRead;
    private final TrustedPreMfaChallengeLifecycleService challengeLifecycle;
    private final TrustedMfaChallengeContextPort challengeContext;
    private final Rfc6238TotpCryptographicVerifier totpVerifier;
    private final Supplier<byte[]> seedMaterialSupplier;
    private final ConcurrentHashMap<PendingTotpEnrollmentId, PendingRecord> pendingById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TrustedPreMfaChallengeId, PendingTotpEnrollmentId> activePendingByChallenge =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PendingTotpEnrollmentId, Object> pendingLocks = new ConcurrentHashMap<>();
    private final EphemeralTotpSeedVault seedVault = new EphemeralTotpSeedVault();

    public SyntheticTotpEnrollmentLifecycleService(
            Clock clock,
            AccountSecuritySnapshotReadPort securityRead,
            TrustedPreMfaChallengeLifecycleService challengeLifecycle,
            TrustedMfaChallengeContextPort challengeContext) {
        this(
                clock,
                securityRead,
                challengeLifecycle,
                challengeContext,
                () -> {
                    var material = new byte[20];
                    new SecureRandom().nextBytes(material);
                    return material;
                });
    }

    SyntheticTotpEnrollmentLifecycleService(
            Clock clock,
            AccountSecuritySnapshotReadPort securityRead,
            TrustedPreMfaChallengeLifecycleService challengeLifecycle,
            TrustedMfaChallengeContextPort challengeContext,
            Supplier<byte[]> seedMaterialSupplier) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.securityRead = Objects.requireNonNull(securityRead, "securityRead");
        this.challengeLifecycle = Objects.requireNonNull(challengeLifecycle, "challengeLifecycle");
        this.challengeContext = Objects.requireNonNull(challengeContext, "challengeContext");
        this.seedMaterialSupplier = Objects.requireNonNull(seedMaterialSupplier, "seedMaterialSupplier");
        this.totpVerifier = new Rfc6238TotpCryptographicVerifier(clock);
    }

    /** Phase 1: start pending enrollment bound to a trusted pre-MFA enrollment challenge. */
    public TotpEnrollmentOutcome beginPending(TrustedPreMfaChallengeId challengeId) {
        Objects.requireNonNull(challengeId, "challengeId");
        var validation = challengeLifecycle.validateForEnrollment(challengeId);
        var denied = mapChallengeDenial(validation);
        if (denied.isPresent()) {
            return denied.get();
        }
        var snapshot = challengeLifecycle.find(challengeId).orElseThrow();
        Object challengeLock = lockForChallenge(challengeId);
        synchronized (challengeLock) {
            var existingId = activePendingByChallenge.get(challengeId);
            if (existingId != null) {
                var existing = pendingById.get(existingId);
                if (existing != null && isActivePending(existing)) {
                    return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_DUPLICATE_PENDING);
                }
                activePendingByChallenge.remove(challengeId, existingId);
            }
            var pendingId = PendingTotpEnrollmentId.generate();
            var authenticatorId = AuthenticatorId.of(java.util.UUID.randomUUID());
            byte[] material = seedMaterialSupplier.get();
            try {
                seedVault.put(pendingId, new TotpSeed(material));
            } finally {
                java.util.Arrays.fill(material, (byte) 0);
            }
            Instant issuedAt = clock.instant();
            PendingRecord record = new PendingRecord(
                    pendingId,
                    authenticatorId,
                    snapshot.subjectUserId(),
                    snapshot.boundAuthRevision(),
                    challengeId,
                    PendingStatus.PENDING,
                    issuedAt,
                    issuedAt.plus(PENDING_TTL));
            if (activePendingByChallenge.putIfAbsent(challengeId, pendingId) != null) {
                seedVault.remove(pendingId);
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_DUPLICATE_PENDING);
            }
            pendingById.put(pendingId, record);
            return TotpEnrollmentOutcome.pendingStarted(pendingId, authenticatorId);
        }
    }

    /** Phase 2: confirm six-digit TOTP; owner bound to server trusted challenge context, not caller-supplied. */
    public TotpEnrollmentOutcome confirm(PendingTotpEnrollmentId pendingId, String totpCode) {
        Objects.requireNonNull(pendingId, "pendingId");
        synchronized (lockForPending(pendingId)) {
            var challengeDenial = resolveTrustedEnrollmentChallengeOrDenial();
            if (challengeDenial.isPresent()) {
                return challengeDenial.get();
            }
            var challenge = challengeLifecycle
                    .find(challengeContext.currentServerChallengeId().orElseThrow())
                    .orElseThrow();
            var record = pendingById.get(pendingId);
            if (record == null) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_FOUND);
            }
            if (!record.challengeId().equals(challenge.challengeId())) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_CHALLENGE_INACTIVE);
            }
            if (!record.ownerUserId().equals(challenge.subjectUserId())) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_WRONG_OWNER);
            }
            if (record.authRevision() != challenge.boundAuthRevision()) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_STALE_AUTH_REVISION);
            }
            if (record.status() != PendingStatus.PENDING) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING);
            }
            if (isExpired(record)) {
                transitionExpired(record);
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_EXPIRED);
            }
            var snapshot = securityRead.currentSnapshot(record.ownerUserId());
            if (snapshot.isEmpty()) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_SUBJECT_UNKNOWN);
            }
            if (snapshot.get().authRevision() != record.authRevision()) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_STALE_AUTH_REVISION);
            }
            var accountDenied = mapAccountStatusToEnrollment(snapshot.get().status());
            if (accountDenied.isPresent()) {
                return accountDenied.get();
            }
            var seedOpt = seedVault.take(pendingId);
            if (seedOpt.isEmpty()) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_FOUND);
            }
            var seed = seedOpt.get();
            boolean cryptoMatch =
                    totpVerifier.findGreatestMatchingStep(seed, totpCode, -1L).isPresent();
            if (!cryptoMatch) {
                if (pendingById.get(pendingId) != null
                        && pendingById.get(pendingId).status() == PendingStatus.PENDING) {
                    seedVault.put(pendingId, seed);
                }
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_INVALID_TOTP);
            }
            transitionConfirmed(record);
            return TotpEnrollmentOutcome.confirmedSynthetic(pendingId, record.authenticatorId());
        }
    }

    public TotpEnrollmentOutcome cancel(PendingTotpEnrollmentId pendingId) {
        Objects.requireNonNull(pendingId, "pendingId");
        synchronized (lockForPending(pendingId)) {
            var challengeDenial = resolveTrustedEnrollmentChallengeOrDenial();
            if (challengeDenial.isPresent()) {
                return challengeDenial.get();
            }
            var challenge = challengeLifecycle
                    .find(challengeContext.currentServerChallengeId().orElseThrow())
                    .orElseThrow();
            var record = pendingById.get(pendingId);
            if (record == null) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_FOUND);
            }
            if (!record.challengeId().equals(challenge.challengeId())) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_CHALLENGE_INACTIVE);
            }
            if (!record.ownerUserId().equals(challenge.subjectUserId())) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_WRONG_OWNER);
            }
            if (record.status() != PendingStatus.PENDING || isExpired(record)) {
                return TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING);
            }
            seedVault.remove(pendingId);
            transitionCancelled(record);
            return TotpEnrollmentOutcome.cancelled(pendingId);
        }
    }

    private Optional<TotpEnrollmentOutcome> resolveTrustedEnrollmentChallengeOrDenial() {
        var challengeId = challengeContext.currentServerChallengeId();
        if (challengeId.isEmpty()) {
            return Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_MISSING_CHALLENGE));
        }
        var validation = challengeLifecycle.validateForEnrollment(challengeId.get());
        var denied = mapChallengeDenial(validation);
        if (denied.isPresent()) {
            return denied;
        }
        if (challengeLifecycle.find(challengeId.get()).isEmpty()) {
            return Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_FOUND));
        }
        return Optional.empty();
    }

    private Optional<TotpEnrollmentOutcome> mapAccountStatusToEnrollment(UserAccountSecurityStatus status) {
        return switch (status) {
            case ENABLED -> Optional.empty();
            case DISABLED -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_DISABLED));
            case LOCKED -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_LOCKED));
            case REVOKED -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_REVOKED));
        };
    }

    private Optional<TotpEnrollmentOutcome> mapChallengeDenial(
            TrustedPreMfaChallengeLifecycleService.ValidationOutcome validation) {
        return switch (validation) {
            case VALID -> Optional.empty();
            case MISSING -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_MISSING_CHALLENGE));
            case NOT_FOUND -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_NOT_FOUND));
            case WRONG_STAGE -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_WRONG_STAGE));
            case EXPIRED -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_EXPIRED));
            case STALE_AUTH_REVISION ->
                    Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_STALE_AUTH_REVISION));
            case DISABLED_SUBJECT -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_DISABLED));
            case LOCKED_SUBJECT -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_LOCKED));
            case REVOKED_SUBJECT -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_REVOKED));
            case SUBJECT_UNKNOWN ->
                    Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_SUBJECT_UNKNOWN));
            default -> Optional.of(TotpEnrollmentOutcome.denied(TotpEnrollmentOutcome.Result.DENIED_CHALLENGE_INACTIVE));
        };
    }

    private void transitionConfirmed(PendingRecord record) {
        activePendingByChallenge.remove(record.challengeId(), record.pendingId());
        pendingById.put(
                record.pendingId(),
                new PendingRecord(
                        record.pendingId(),
                        record.authenticatorId(),
                        record.ownerUserId(),
                        record.authRevision(),
                        record.challengeId(),
                        PendingStatus.CONFIRMED_SYNTHETIC,
                        record.issuedAt(),
                        record.expiresAt()));
        pendingLocks.remove(record.pendingId());
    }

    private void transitionCancelled(PendingRecord record) {
        activePendingByChallenge.remove(record.challengeId(), record.pendingId());
        pendingById.put(
                record.pendingId(),
                new PendingRecord(
                        record.pendingId(),
                        record.authenticatorId(),
                        record.ownerUserId(),
                        record.authRevision(),
                        record.challengeId(),
                        PendingStatus.CANCELLED,
                        record.issuedAt(),
                        record.expiresAt()));
        pendingLocks.remove(record.pendingId());
    }

    private void transitionExpired(PendingRecord record) {
        seedVault.remove(record.pendingId());
        activePendingByChallenge.remove(record.challengeId(), record.pendingId());
        pendingById.put(
                record.pendingId(),
                new PendingRecord(
                        record.pendingId(),
                        record.authenticatorId(),
                        record.ownerUserId(),
                        record.authRevision(),
                        record.challengeId(),
                        PendingStatus.EXPIRED,
                        record.issuedAt(),
                        record.expiresAt()));
        pendingLocks.remove(record.pendingId());
    }

    private boolean isActivePending(PendingRecord record) {
        return record.status() == PendingStatus.PENDING && !isExpired(record);
    }

    private boolean isExpired(PendingRecord record) {
        return !clock.instant().isBefore(record.expiresAt());
    }

    private Object lockForPending(PendingTotpEnrollmentId pendingId) {
        return pendingLocks.computeIfAbsent(pendingId, id -> new Object());
    }

    private static final ConcurrentHashMap<TrustedPreMfaChallengeId, Object> CHALLENGE_LOCKS =
            new ConcurrentHashMap<>();

    private static Object lockForChallenge(TrustedPreMfaChallengeId challengeId) {
        return CHALLENGE_LOCKS.computeIfAbsent(challengeId, id -> new Object());
    }

    record PendingRecord(
            PendingTotpEnrollmentId pendingId,
            AuthenticatorId authenticatorId,
            UserId ownerUserId,
            long authRevision,
            TrustedPreMfaChallengeId challengeId,
            PendingStatus status,
            Instant issuedAt,
            Instant expiresAt) {

        @Override
        public String toString() {
            return "PendingTotpEnrollment{pendingId="
                    + pendingId
                    + ", authenticatorId="
                    + authenticatorId
                    + ", ownerUserId="
                    + ownerUserId
                    + ", authRevision="
                    + authRevision
                    + ", status="
                    + status
                    + "}";
        }
    }

    static final class EphemeralTotpSeedVault {
        private final ConcurrentHashMap<PendingTotpEnrollmentId, TotpSeed> seeds = new ConcurrentHashMap<>();

        void put(PendingTotpEnrollmentId id, TotpSeed seed) {
            seeds.put(id, seed);
        }

        Optional<TotpSeed> take(PendingTotpEnrollmentId id) {
            return Optional.ofNullable(seeds.remove(id));
        }

        void remove(PendingTotpEnrollmentId id) {
            seeds.remove(id);
        }

        boolean hasSeed(PendingTotpEnrollmentId id) {
            return seeds.containsKey(id);
        }

        @Override
        public String toString() {
            return "EphemeralTotpSeedVault[count=" + seeds.size() + "]";
        }
    }
}