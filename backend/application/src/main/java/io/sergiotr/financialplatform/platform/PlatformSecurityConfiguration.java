package io.sergiotr.financialplatform.platform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Deny-by-default HTTP surface: health probes only (no business REST in V0/V1 substrate). */
@Configuration
@EnableWebSecurity
class PlatformSecurityConfiguration {

    @Bean
    SecurityFilterChain platformSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .anyRequest()
                        .denyAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}