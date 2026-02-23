package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class AssignPermissionToRoleRequestDTO {
    private String roleId;
    private String permissionId;
}