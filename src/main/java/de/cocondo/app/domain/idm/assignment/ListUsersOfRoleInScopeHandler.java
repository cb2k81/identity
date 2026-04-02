// Datei: src/main/java/de/cocondo/app/domain/idm/assignment/ListUsersOfRoleInScopeHandler.java
package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersOfRoleInScopeHandler {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final RoleEntityService roleEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public List<UserAccountDTO> handle(String roleId, String applicationKey, String stageKey) {

        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }
        if (applicationKey == null || applicationKey.isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (stageKey == null || stageKey.isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ApplicationScope not found"));

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (role.getApplicationScope() == null || !scope.getId().equals(role.getApplicationScope().getId())) {
            throw new IllegalArgumentException("Role does not belong to requested scope");
        }

        return userRoleAssignmentEntityService
                .loadAllByRoleIdAndScope(roleId, applicationKey, stageKey)
                .stream()
                .map(UserRoleAssignment::getUserAccount)
                .filter(user -> user != null)
                .map(user -> {
                    UserAccountDTO dto = new UserAccountDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setDisplayName(user.getDisplayName());
                    dto.setEmail(user.getEmail());
                    dto.setState(user.getState());
                    return dto;
                })
                .toList();
    }
}