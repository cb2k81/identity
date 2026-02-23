package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class CreatePermissionGroupRequestDTO {
    private String applicationScopeId;
    private String name;
    private String description;
}