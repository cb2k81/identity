package de.cocondo.app.domain.idm.permission.dto;

import lombok.Data;

@Data
public class CreatePermissionRequestDTO {
    private String applicationScopeId;
    private String permissionGroupId;
    private String name;
    private String description;
    private boolean systemProtected;
}