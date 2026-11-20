package io.sergiotr.financialplatform.identity.runtime;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.env.Profiles;

/**
 * Fail-closed: synthetic identity beans require {@code dev-synthetic} and must not load alongside production-like
 * profiles ({@code prod}, {@code production}).
 */
final class DevSyntheticExclusiveProfileCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var env = context.getEnvironment();
        if (!env.acceptsProfiles(Profiles.of("dev-synthetic"))) {
            return false;
        }
        return !env.acceptsProfiles(Profiles.of("prod", "production"));
    }
}