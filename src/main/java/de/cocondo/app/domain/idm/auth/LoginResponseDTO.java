package de.cocondo.app.domain.idm.auth;

import lombok.Data;

/**
 * Login response payload.
 *
 * Contains issued JWT and metadata.
 *
 * NOTE:
 * - No password hash exposed
 * - No domain entity returned
 */
@Data
public class LoginResponseDTO {

    private String token;

    private String tokenType = "Bearer";

    private long expiresAt;
}