package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

/**
 * Request DTO for authenticating a user in a specific ApplicationScope context.
 */
@Data
public class AuthenticateUserRequestDTO {

    private String username;

    private String password;

    /**
     * Application key, e.g. "IDM", "CRM".
     */
    private String applicationKey;

    /**
     * Stage key, e.g. "DEV", "TEST", "PROD".
     */
    private String stageKey;
}