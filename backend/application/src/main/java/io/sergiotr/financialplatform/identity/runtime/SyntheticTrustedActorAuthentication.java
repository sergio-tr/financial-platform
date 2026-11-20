package io.sergiotr.financialplatform.identity.runtime;

import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

/** Spring Security principal carrying a trusted synthetic workspace actor (test/DEV injection only). */
public final class SyntheticTrustedActorAuthentication extends AbstractAuthenticationToken {

    private final TrustedWorkspaceActor actor;

  public SyntheticTrustedActorAuthentication(TrustedWorkspaceActor actor) {
        super(authoritiesFor(actor));
        this.actor = actor;
        setAuthenticated(actor.hasWorkspaceAssurance());
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return actor;
    }

    public TrustedWorkspaceActor actor() {
        return actor;
    }

    private static Collection<? extends GrantedAuthority> authoritiesFor(TrustedWorkspaceActor actor) {
        if (actor.assurance() == SecurityAssuranceStatus.AUTHENTICATED_FULL) {
            return AuthorityUtils.createAuthorityList("ROLE_SYNTHETIC_WORKSPACE");
        }
        return List.of();
    }
}