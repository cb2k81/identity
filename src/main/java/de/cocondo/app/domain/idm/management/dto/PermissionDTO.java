package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private String id;
    private String applicationScopeId;
    private String permissionGroupId;
    private String name;
    private String description;
    private boolean systemProtected;
}