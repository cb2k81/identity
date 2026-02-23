package de.cocondo.app.system.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Security configuration (DEV profile).
 *
 * Responsibility:
 * - Configure HTTP-level security (filter chain)
 * - No authentication required in development
 * - All endpoints accessible
 *
 * Important:
 * - This configuration must NOT be used in production.
 * - Production will use JWT Resource Server configuration.
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class HttpSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpSecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        logger.info("Initializing DEV HttpSecurity configuration (all endpoints open)");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
