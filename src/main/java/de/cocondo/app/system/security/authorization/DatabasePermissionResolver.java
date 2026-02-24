package de.cocondo.app.system.security.authorization;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Default PermissionResolver implementation backed by the IDM database.
 *
 * Strategy:
 * - Resolve ApplicationScope by (applicationKey, stageKey)
 * - Resolve Role entities by (scopeId, roleName)
 * - Resolve RolePermissionAssignments and return permission names as authorities
 */
@Service
@RequiredArgsConstructor
public class DatabasePermissionResolver implements PermissionResolver {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final RoleEntityService roleEntityService;
    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

    @Override
    public List<String> resolveAuthorities(String applicationKey, String stageKey, List<String> roleNames) {

        if (applicationKey == null || applicationKey.isBlank()) {
            return Collections.emptyList();
        }
        if (stageKey == null || stageKey.isBlank()) {
            return Collections.emptyList();
        }
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptyList();
        }

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElse(null);

        if (scope == null) {
            return Collections.emptyList();
        }

        return roleNames.stream()
                .filter(rn -> rn != null && !rn.isBlank())
                .map(String::trim)
                .distinct()
                .map(roleName -> roleEntityService.loadByApplicationScopeIdAndName(scope.getId(), roleName).orElse(null))
                .filter(r -> r != null)
                .map(Role::getId)
                .distinct()
                .flatMap(roleId -> rolePermissionAssignmentEntityService
                        .loadAllByRoleId(roleId)
                        .stream()
                        .map(a -> a.getPermission().getName()))
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}