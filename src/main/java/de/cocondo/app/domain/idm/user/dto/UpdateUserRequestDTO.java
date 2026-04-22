package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

/**
 * Request DTO for updating editable user account master data.
 *
 * Minimal update contract for the current UI phase.
 */
@Data
public class UpdateUserRequestDTO {

    private String displayName;

    private String email;
}