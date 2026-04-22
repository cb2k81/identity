// Datei: src/main/java/de/cocondo/app/domain/idm/user/dto/UserAccountDTO.java
package de.cocondo.app.domain.idm.user.dto;

import de.cocondo.app.domain.idm.user.UserAccountState;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO representing a user account outside the domain aggregate.
 */
@Data
public class UserAccountDTO {

    private String id;

    private String username;

    private String displayName;

    private String email;

    private UserAccountState state;

    private int failedLoginAttempts;

    private Instant lockedUntil;

    private String createdBy;

    private LocalDateTime createdAt;

    private String lastModifiedBy;

    private LocalDateTime lastModifiedAt;

    private long loginCount;

    private LocalDateTime lastLogin;
}