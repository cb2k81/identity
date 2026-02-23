package de.cocondo.app.domain.idm.auth;

import lombok.Data;

/**
 * Login request payload.
 *
 * Contains raw credentials.
 *
 * NOTE:
 * - No technical ID included
 * - Password is never stored, only used transiently
 */
@Data
public class LoginRequestDTO {

    private String username;

    private String password;
}