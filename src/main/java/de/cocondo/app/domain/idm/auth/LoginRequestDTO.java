package de.cocondo.app.domain.idm.auth;

import lombok.Data;

/**
 * Login request payload.
 *
 * Contains raw credentials and scope context.
 *
 * NOTE:
 * - Password is never stored, only used transiently
 * - Authentication is scope-aware (applicationKey + stageKey)
 */
@Data
public class LoginRequestDTO {

    private String username;

    private String password;

    private String applicationKey;

    private String stageKey;
}