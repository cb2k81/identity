package de.cocondo.app.domain.idm.role.dto;

import lombok.Data;

@Data
public class CreateRoleRequestDTO {
    private String applicationScopeId;
    private String name;
    private String description;
    private boolean systemProtected;
}