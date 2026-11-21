package io.sergiotr.financialplatform.identity.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.domain.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextWorkspaceActorAdapterTest {

    private final SecurityContextWorkspaceActorAdapter adapter = new SecurityContextWorkspaceActorAdapter();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ignoresUnauthenticatedTrustedPrincipal() {
        var actor = new TrustedWorkspaceActor(UserId.parse("10000000-0000-4000-8000-000000000001"), 1L,
                SecurityAssuranceStatus.AUTHENTICATED_FULL);
        var token = new UsernamePasswordAuthenticationToken(actor, "n/a", java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        assertThat(adapter.currentActor()).isEmpty();
    }

    @Test
    void acceptsAuthenticatedSyntheticToken() {
        var actor = new TrustedWorkspaceActor(UserId.parse("10000000-0000-4000-8000-000000000001"), 1L,
                SecurityAssuranceStatus.AUTHENTICATED_FULL);
        SecurityContextHolder.getContext().setAuthentication(new SyntheticTrustedActorAuthentication(actor));

        assertThat(adapter.currentActor()).contains(actor);
    }

    @Test
    void exposesPreMfaSyntheticTokenForInsufficientAssuranceDeny() {
        var actor = new TrustedWorkspaceActor(UserId.parse("10000000-0000-4000-8000-000000000001"), 1L,
                SecurityAssuranceStatus.PRE_MFA);
        SecurityContextHolder.getContext().setAuthentication(new SyntheticTrustedActorAuthentication(actor));

        assertThat(adapter.currentActor()).contains(actor);
    }
}