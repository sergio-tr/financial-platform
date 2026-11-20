package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.Optional;

public record MembershipMutationResult(
        boolean applied,
        Optional<AuthorizationDenialReason> authorizationDenial,
        Optional<MembershipMutationDenialReason> mutationDenial,
        Optional<WorkspaceMembership> membership) {

    public MembershipMutationResult {
        Objects.requireNonNull(authorizationDenial, "authorizationDenial");
        Objects.requireNonNull(mutationDenial, "mutationDenial");
        Objects.requireNonNull(membership, "membership");
    }

    public static MembershipMutationResult applied(WorkspaceMembership membership) {
        return new MembershipMutationResult(
                true, Optional.empty(), Optional.empty(), Optional.of(membership));
    }

    public static MembershipMutationResult authorizationDenied(AuthorizationDenialReason reason) {
        return new MembershipMutationResult(false, Optional.of(reason), Optional.empty(), Optional.empty());
    }

    public static MembershipMutationResult mutationDenied(MembershipMutationDenialReason reason) {
        return new MembershipMutationResult(false, Optional.empty(), Optional.of(reason), Optional.empty());
    }
}