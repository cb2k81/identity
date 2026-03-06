package de.cocondo.app.system.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurable HTTP security paths for the system kernel.
 *
 * Allows separating generic security concepts (e.g. "/api/**" secured)
 * from application-specific endpoints (e.g. "/auth/login").
 *
 * system.security.http.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "system.security.http")
public class HttpSecurityPathsProperties {

    /**
     * POST endpoints that should be publicly accessible (permitAll).
     * Example (IDM): "/auth/login"
     */
    private List<String> permitAllPostPaths = new ArrayList<>();

    /**
     * Paths that should be publicly accessible (permitAll).
     * Defaults cover Swagger and static resources.
     */
    private List<String> permitAllPaths = new ArrayList<>(List.of(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/",
            "/index.html",
            "/favicon.ico",
            "/static/**"
    ));

    /**
     * Paths that require authentication.
     * Default: protect technical APIs under /api/**
     */
    private List<String> authenticatedPaths = new ArrayList<>(List.of(
            "/api/**"
    ));
}