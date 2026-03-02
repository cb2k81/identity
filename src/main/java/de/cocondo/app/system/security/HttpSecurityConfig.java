package de.cocondo.app.system.security;

import de.cocondo.app.system.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * HTTP Security configuration (ALL profiles: dev/test/prod).
 *
 * Responsibility:
 * - Configure HTTP-level security (filter chain)
 * - JWT protected API under /api/**
 * - Login endpoint always reachable without authentication
 *
 * Notes:
 * - Differences between environments must be handled via properties, not profiles.
 */
@Configuration
@EnableWebSecurity
public class HttpSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpSecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {

        logger.info("Initializing HttpSecurity configuration (JWT protected, all profiles)");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ------------------------------------------------------------
                        // Public endpoints (must stay reachable without authentication)
                        // ------------------------------------------------------------

                        // login (support both paths: /api/auth/login and /api/idm/auth/login)
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/idm/auth/login"
                        ).permitAll()

                        // springdoc defaults (see EndpointPrinter)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // static / landing (system DefaultController + resources)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/static/**"
                        ).permitAll()

                        // PUBLIC API (anonymous allowed)
                        .requestMatchers("/public/**").permitAll()

                        // ------------------------------------------------------------
                        // Protected API
                        // ------------------------------------------------------------
                        .requestMatchers("/api/**").authenticated()

                        // remaining endpoints (e.g. actuator base path etc.)
                        .anyRequest().permitAll()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}