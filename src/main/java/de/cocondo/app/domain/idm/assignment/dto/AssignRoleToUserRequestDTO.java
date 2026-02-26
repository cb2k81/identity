package de.cocondo.app.domain.idm.assignment.dto;

import lombok.Data;

@Data
public class AssignRoleToUserRequestDTO {
    private String userAccountId;
    private String roleId;
}