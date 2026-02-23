package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

/**
 * DTO returned after successful authentication.
 *
 * Contains only the identity data needed by downstream services (e.g. token issuing).
 */
@Data
public class AuthenticatedUserDTO {

    private String id;

    private String username;
}