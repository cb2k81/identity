package de.cocondo.app.domain.idm.management.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private String id;
    private String applicationScopeId;
    private String name;
    private String description;
    private boolean systemProtected;
}