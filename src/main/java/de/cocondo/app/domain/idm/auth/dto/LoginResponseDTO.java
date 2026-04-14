package de.cocondo.app.domain.idm.auth.dto;

import lombok.Data;

/**
 * Login response payload.
 *
 * Contains issued JWT and metadata.
 *
 * NOTE:
 * - No password hash exposed
 * - No domain entity returned
 * - Refresh data is added additively to keep the existing client contract stable
 */
@Data
public class LoginResponseDTO {

    private String token;

    private String tokenType = "Bearer";

    private long expiresAt;

    /**
     * Opaque refresh token / renew token for the server-controlled auth session.
     *
     * This value is intended for client storage and later use in refresh / logout flows.
     */
    private String refreshToken;

    /**
     * Absolute expiration timestamp (epoch millis) of the refresh token / auth session.
     */
    private long refreshExpiresAt;
}