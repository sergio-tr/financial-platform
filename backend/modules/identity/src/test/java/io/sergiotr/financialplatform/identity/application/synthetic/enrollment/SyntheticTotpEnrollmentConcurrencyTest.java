package io.sergiotr.financialplatform.identity.application.synthetic.enrollment;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthFixtureSupport;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.application.synthetic.totp.Rfc6238TotpCryptographicVerifier;
import io.sergiotr.financialplatform.identity.domain.PendingTotpEnrollmentId;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaChallengeContextPort;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SyntheticTotpEnrollmentConcurrencyTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    @Test
    void concurrentBeginPendingAllowsAtMostOneActivePerChallenge() throws Exception {
        var clock = Clock.fixed(Instant.parse("2026-11-21T10:00:00Z"), ZoneOffset.UTC);
        var revisionStore = new InMemoryAccountSecurityAuthRevisionStore();
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 2L);
        var challengeLifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisionStore);
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        TrustedPreMfaChallengeId challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var bound = new AtomicReference<Optional<TrustedPreMfaChallengeId>>(Optional.of(challengeId));
        var enrollment = new SyntheticTotpEnrollmentLifecycleService(
                clock, revisionStore, challengeLifecycle, bound::get, () -> RFC_SECRET.clone());

        int threads = 8;
        var latch = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(threads);
        var started = new AtomicInteger(0);
        var duplicate = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                latch.await();
                var outcome = enrollment.beginPending(challengeId);
                if (outcome.result() == TotpEnrollmentOutcome.Result.PENDING_STARTED) {
                    started.incrementAndGet();
                } else if (outcome.result() == TotpEnrollmentOutcome.Result.DENIED_DUPLICATE_PENDING) {
                    duplicate.incrementAndGet();
                }
                return null;
            }));
        }
        latch.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdownNow();
        assertEquals(1, started.get(), "exactly one pending per challenge");
        assertEquals(threads - 1, duplicate.get());
    }

    @Test
    void confirmAndCancelRaceLeavesMonotonicTerminalState() throws Exception {
        var clock = Clock.fixed(Instant.parse("2026-11-21T10:00:00Z"), ZoneOffset.UTC);
        var revisionStore = new InMemoryAccountSecurityAuthRevisionStore();
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 2L);
        var challengeLifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisionStore);
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        TrustedPreMfaChallengeId challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var bound = new AtomicReference<Optional<TrustedPreMfaChallengeId>>(Optional.of(challengeId));
        var enrollment = new SyntheticTotpEnrollmentLifecycleService(
                clock, revisionStore, challengeLifecycle, bound::get, () -> RFC_SECRET.clone());
        var started = enrollment.beginPending(challengeId);
        PendingTotpEnrollmentId pendingId = started.pendingId().orElseThrow();
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format(
                "%06d",
                verifier.codeAtEpochSecond(new TotpSeed(RFC_SECRET), clock.instant().getEpochSecond()).orElseThrow());

        var latch = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        var confirmResult = new AtomicReference<TotpEnrollmentOutcome.Result>();
        var cancelResult = new AtomicReference<TotpEnrollmentOutcome.Result>();
        Future<?> confirm =
                pool.submit(() -> {
                    latch.await();
                    confirmResult.set(enrollment.confirm(pendingId, code).result());
                    return null;
                });
        Future<?> cancel = pool.submit(() -> {
            latch.await();
            cancelResult.set(enrollment.cancel(pendingId).result());
            return null;
        });
        latch.countDown();
        confirm.get();
        cancel.get();
        pool.shutdownNow();

        var terminal = List.of(confirmResult.get(), cancelResult.get());
        assertTrue(
                terminal.contains(TotpEnrollmentOutcome.Result.CONFIRMED_SYNTHETIC)
                        || terminal.contains(TotpEnrollmentOutcome.Result.CANCELLED));
        assertTrue(
                terminal.contains(TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING)
                        || terminal.stream().filter(r -> r == TotpEnrollmentOutcome.Result.CONFIRMED_SYNTHETIC
                                        || r == TotpEnrollmentOutcome.Result.CANCELLED)
                                .count()
                        == 1);
        var afterConfirm = enrollment.confirm(pendingId, code);
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING, afterConfirm.result());
    }
}