package de.cocondo.app.system.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables method-level security.
 *
 * Responsibilities:
 * - Activates @PreAuthorize / @PostAuthorize
 * - Activates authorization checks on service-layer methods
 *
 * HTTP security is configured separately.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
