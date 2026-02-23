package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.system.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
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
 * - May reference domain classes (e.g., UserAccount)
 * - MUST NOT implement signing logic (delegates to JwtService)
 * - MUST NOT implement HTTP or controller logic
 */
@Service
@RequiredArgsConstructor
public class IdmTokenService {

    private final JwtService jwtService;

    /**
     * Issues a signed JWT for the given authenticated user.
     *
     * @param user authenticated UserAccount
     * @return signed JWT string
     */
    public String issueToken(UserAccount user) {

        Map<String, Object> claims = new HashMap<>();

        // TODO-ARCH: Extend claims with roles/permissions when domain model evolves
        // Minimal MVP claim set:

        claims.put("sub", user.getId());          // technical identity
        claims.put("username", user.getUsername());

        Date expiration = calculateExpiration();

        return jwtService.generateToken(claims, expiration);
    }

    /**
     * Defines token expiration policy.
     *
     * TODO-ARCH: Externalize TTL via configuration.
     */
    private Date calculateExpiration() {

        long now = System.currentTimeMillis();

        // 1 hour validity (MVP default)
        long ttlMillis = 60 * 60 * 1000;

        return new Date(now + ttlMillis);
    }
}