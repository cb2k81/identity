package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

/**
 * Request DTO for changing a user's password.
 */
@Data
public class ChangePasswordRequestDTO {

    private String newPassword;
}