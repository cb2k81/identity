package de.cocondo.app.domain.idm.user.dto;

import de.cocondo.app.domain.idm.user.UserAccountState;
import lombok.Data;

/**
 * DTO representing a user account outside the domain aggregate.
 */
@Data
public class UserAccountDTO {

    private String id;

    private String username;

    private UserAccountState state;
}