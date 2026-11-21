package io.sergiotr.financialplatform.identity.runtime;

import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.port.WorkspaceActorContextPort;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Maps Spring Security context to {@link TrustedWorkspaceActor}; ignores transport headers and caller-supplied IDs. */
final class SecurityContextWorkspaceActorAdapter implements WorkspaceActorContextPort {

    @Override
    public Optional<TrustedWorkspaceActor> currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication instanceof SyntheticTrustedActorAuthentication synthetic) {
            return Optional.of(synthetic.actor());
        }
        if (!authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}