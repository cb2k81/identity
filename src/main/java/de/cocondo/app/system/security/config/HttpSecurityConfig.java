package de.cocondo.app.system.security.config;

import de.cocondo.app.system.security.config.HttpSecurityPathsProperties;
import de.cocondo.app.system.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(HttpSecurityPathsProperties.class)
public class HttpSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpSecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint restAuthenticationEntryPoint,
            AccessDeniedHandler restAccessDeniedHandler,
            HttpSecurityPathsProperties paths
    ) throws Exception {

        logger.info("Initializing HttpSecurity configuration (JWT protected, all profiles)");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    // public POST endpoints (app-specific, e.g. login)
                    for (String p : paths.getPermitAllPostPaths()) {
                        if (p != null && !p.isBlank()) {
                            auth.requestMatchers(HttpMethod.POST, p).permitAll();
                        }
                    }

                    // public paths (generic defaults: swagger + static)
                    if (paths.getPermitAllPaths() != null && !paths.getPermitAllPaths().isEmpty()) {
                        auth.requestMatchers(paths.getPermitAllPaths().toArray(new String[0])).permitAll();
                    }

                    // protected paths (generic default: /api/**)
                    for (String p : paths.getAuthenticatedPaths()) {
                        if (p != null && !p.isBlank()) {
                            auth.requestMatchers(p).authenticated();
                        }
                    }

                    auth.anyRequest().permitAll();
                })
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