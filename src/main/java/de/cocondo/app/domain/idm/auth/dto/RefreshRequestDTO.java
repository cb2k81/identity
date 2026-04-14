package de.cocondo.app.domain.idm.auth.dto;

import lombok.Data;

/**
 * Refresh request payload.
 *
 * Contains the opaque refresh token / renew token that references
 * the server-side auth session.
 */
@Data
public class RefreshRequestDTO {

    private String refreshToken;
}