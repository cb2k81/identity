package de.cocondo.app.system.security;

import de.cocondo.app.system.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * HTTP Security configuration (NON-DEV).
 */
@Configuration
@EnableWebSecurity
@Profile("!dev")
public class HttpSecurityJwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpSecurityJwtConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {

        logger.info("Initializing NON-DEV HttpSecurity configuration (JWT protected)");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers(
                                "/api/auth/login",

                                // springdoc defaults (see EndpointPrinter)
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                // static / landing (system DefaultController + resources)
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/static/**",

                                // PUBLIC API (anonymous allowed)
                                "/public/**"
                        ).permitAll()

                        // everything else under /api is protected
                        .requestMatchers("/api/**").authenticated()

                        // remaining endpoints
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