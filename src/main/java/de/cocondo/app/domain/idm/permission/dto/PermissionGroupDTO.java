package de.cocondo.app.domain.idm.permission.dto;

import lombok.Data;

@Data
public class PermissionGroupDTO {
    private String id;
    private String applicationScopeId;
    private String name;
    private String description;
}