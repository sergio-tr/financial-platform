package io.sergiotr.financialplatform.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "financial-platform.runtime")
public class RuntimeRoleProperties {

    private RuntimeRole role = RuntimeRole.ALL;

    public RuntimeRole getRole() {
        return role;
    }

    public void setRole(RuntimeRole role) {
        this.role = role;
    }
}