package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import java.util.Optional;

/** Resolves the current actor from a trusted server security context (synthetic/test injection in V1). */
public interface WorkspaceActorContextPort {

    Optional<TrustedWorkspaceActor> currentActor();
}