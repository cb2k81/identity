// Datei: src/main/java/de/cocondo/app/domain/idm/user/dto/CreateUserRequestDTO.java
package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

/**
 * Request DTO for creating a new user account.
 *
 * Domain-service boundary object (no entities).
 */
@Data
public class CreateUserRequestDTO {

    private String username;

    private String displayName;

    private String email;

    private String password;
}