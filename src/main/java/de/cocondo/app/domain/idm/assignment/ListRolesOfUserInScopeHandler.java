package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesOfUserInScopeHandler {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    public List<RoleDTO> handle(String userAccountId, String applicationKey, String stageKey) {

        if (userAccountId == null || userAccountId.isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }
        if (applicationKey == null || applicationKey.isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (stageKey == null || stageKey.isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        return userRoleAssignmentEntityService
                .loadAllByUserAccountIdAndScope(userAccountId, applicationKey, stageKey)
                .stream()
                .map(UserRoleAssignment::getRole)
                .filter(role -> role != null)
                .filter(role -> role.getApplicationScope() != null)
                .filter(role -> scope.getId().equals(role.getApplicationScope().getId()))
                .map(role -> {
                    RoleDTO dto = new RoleDTO();
                    dto.setId(role.getId());
                    dto.setApplicationScopeId(role.getApplicationScope().getId());
                    dto.setName(role.getName());
                    dto.setDescription(role.getDescription());
                    dto.setSystemProtected(role.isSystemProtected());
                    return dto;
                })
                .toList();
    }
}