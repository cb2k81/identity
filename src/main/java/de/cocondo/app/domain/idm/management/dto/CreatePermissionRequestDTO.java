package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class CreatePermissionRequestDTO {
    private String applicationScopeId;
    private String permissionGroupId;
    private String name;
    private String description;
    private boolean systemProtected;
}