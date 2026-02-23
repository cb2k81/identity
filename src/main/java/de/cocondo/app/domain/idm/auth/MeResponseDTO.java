package de.cocondo.app.domain.idm.auth;

import lombok.Data;

/**
 * Response DTO for /api/auth/me endpoint.
 *
 * Contains minimal identity information extracted
 * from the authenticated principal.
 */
@Data
public class MeResponseDTO {

    private String username;
}