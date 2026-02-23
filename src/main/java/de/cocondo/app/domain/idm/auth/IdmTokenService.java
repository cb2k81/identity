package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import de.cocondo.app.system.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * IDM-specific token issuing service.
 *
 * Responsibilities:
 * - Define JWT claim structure for IDM
 * - Define subject mapping
 * - Define token lifetime (TTL)
 *
 * Architectural constraints:
 * - MUST NOT implement signing logic (delegates to JwtService)
 * - MUST NOT implement HTTP or controller logic
 * - MUST NOT require domain entities as input
 */
@Service
@RequiredArgsConstructor
public class IdmTokenService {

    private final JwtService jwtService;

    @Value("${idm.security.jwt.ttl-ms}")
    private long ttlMillis;

    /**
     * Value object representing an issued token.
     *
     * @param token     signed JWT
     * @param expiresAt absolute expiration timestamp (epoch millis)
     */
    public record IssuedToken(String token, long expiresAt) {
    }

    public IssuedToken issueToken(AuthenticatedUserDTO user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", user.getId());
        claims.put("username", user.getUsername());

        Date expiration = calculateExpiration();
        String token = jwtService.generateToken(claims, expiration);

        return new IssuedToken(token, expiration.getTime());
    }

    private Date calculateExpiration() {

        long now = System.currentTimeMillis();
        return new Date(now + ttlMillis);
    }
}