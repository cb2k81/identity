package de.cocondo.app.domain.idm.assignment.dto;

import lombok.Data;

@Data
public class AssignApplicationScopeToUserRequestDTO {
    private String userAccountId;
    private String applicationScopeId;
}