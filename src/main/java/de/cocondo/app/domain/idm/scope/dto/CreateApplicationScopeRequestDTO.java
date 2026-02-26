package de.cocondo.app.domain.idm.scope.dto;

import lombok.Data;

@Data
public class CreateApplicationScopeRequestDTO {
    private String applicationKey;
    private String stageKey;
    private String description;
}