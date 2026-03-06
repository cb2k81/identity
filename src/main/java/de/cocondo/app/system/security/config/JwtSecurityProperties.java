package de.cocondo.app.system.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration for the system security kernel.
 *
 * Prefix:
 * system.security.jwt
 *
 * Responsibilities:
 * - Provide secret for JWT signing
 * - Provide TTL configuration
 *
 * Pure technical configuration.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "system.security.jwt")
public class JwtSecurityProperties {

    /**
     * Secret used for JWT signing (HS256).
     * Must be at least 32 bytes.
     */
    private String secret;

    /**
     * Token TTL in milliseconds.
     */
    private long ttlMs;
}