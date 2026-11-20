package io.sergiotr.financialplatform.platform;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(RuntimeRoleProperties.class)
public class PlatformRuntimeConfiguration {}