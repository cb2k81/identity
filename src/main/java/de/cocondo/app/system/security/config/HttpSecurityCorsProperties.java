package de.cocondo.app.system.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurable CORS settings for HTTP security.
 *
 * CORS is handled centrally in HttpSecurityConfig to avoid conflicting
 * registrations between Spring MVC and Spring Security.
 *
 * system.security.cors.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "system.security.cors")
public class HttpSecurityCorsProperties {

    /**
     * Allowed origins for cross-origin requests.
     * Example (DEV/TEST): "http://localhost:5175"
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Allowed HTTP methods for cross-origin requests.
     */
    private List<String> allowedMethods = new ArrayList<>(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    /**
     * Allowed request headers for cross-origin requests.
     */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    /**
     * Response headers exposed to the browser.
     */
    private List<String> exposedHeaders = new ArrayList<>(List.of("*"));

    /**
     * Whether credentials (cookies / authorization headers) are allowed.
     *
     * For browser JWT login flows with Authorization header handling,
     * this is required in the current DEV/TEST setup.
     */
    private boolean allowCredentials = true;
}