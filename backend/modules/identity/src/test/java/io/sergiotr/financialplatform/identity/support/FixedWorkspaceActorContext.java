package io.sergiotr.financialplatform.identity.support;

import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.port.WorkspaceActorContextPort;
import java.util.Optional;

/** Test-only injection of trusted actor context. */
public final class FixedWorkspaceActorContext implements WorkspaceActorContextPort {

    private TrustedWorkspaceActor actor;

    public void setActor(TrustedWorkspaceActor actor) {
        this.actor = actor;
    }

    public void clear() {
        this.actor = null;
    }

    @Override
    public Optional<TrustedWorkspaceActor> currentActor() {
        return Optional.ofNullable(actor);
    }
}