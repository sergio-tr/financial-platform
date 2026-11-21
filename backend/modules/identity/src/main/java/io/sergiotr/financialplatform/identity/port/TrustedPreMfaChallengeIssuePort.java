package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserId;

/**
 * Issues opaque pre-MFA challenges after server-verified primary auth. Implementation must reject
 * caller-forged identifiers.
 */
public interface TrustedPreMfaChallengeIssuePort {

    TrustedPreMfaChallengeId issue(
            UserId subjectUserId, long authRevision, AuthenticationStage stageAfterPrimaryVerified);
}