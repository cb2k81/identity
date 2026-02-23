package de.cocondo.app.system.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

// TODO-ARCH: Secret must be externalized via configuration (application.yml)
// TODO-ARCH: This service must remain domain-agnostic (no domain imports allowed)
// TODO-ARCH: Claim construction must be implemented in domain layer

/**
 * Technical JWT service.
 *
 * This class is part of the technical shared kernel (system package).
 *
 * Responsibilities:
 * - Sign JWT tokens
 * - Validate JWT tokens
 * - Parse claims
 *
 * Architectural constraints:
 * - MUST NOT reference any domain classes
 * - MUST NOT define which claims are required
 * - MUST NOT implement business rules
 * - MUST remain reusable across multiple applications
 *
 * Claim construction and semantic meaning are defined in the domain layer.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;

    /**
     * Minimal MVP constructor.
     *
     * NOTE:aaaa
     * In production this key must come from secure configuration.
     * For MVP it is hardcoded but should be externalized later.
     */
    public JwtService() {
        // IMPORTANT:
        // This is a placeholder key for MVP.
        // Replace with externalized configuration before production use.
        String secret = "change-this-secret-key-change-this-secret-key";
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Signs a JWT using provided claims and expiration timestamp.
     *
     * @param claims claims to include in token
     * @param expiration expiration timestamp
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> claims, Date expiration) {

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses and validates a JWT.
     *
     * @param token JWT string
     * @return parsed Claims
     * @throws JwtException if token is invalid
     */
    public Claims parseToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}