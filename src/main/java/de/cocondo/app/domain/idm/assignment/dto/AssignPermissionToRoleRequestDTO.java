package de.cocondo.app.domain.idm.assignment.dto;

import lombok.Data;

@Data
public class AssignPermissionToRoleRequestDTO {
    private String roleId;
    private String permissionId;
}