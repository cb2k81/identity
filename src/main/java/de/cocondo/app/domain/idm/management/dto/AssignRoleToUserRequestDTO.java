package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class AssignRoleToUserRequestDTO {
    private String userAccountId;
    private String roleId;
}