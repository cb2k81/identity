package de.cocondo.app.domain.idm.security;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.system.security.authorization.PermissionAuthoritySource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * IDM-specific PermissionAuthoritySource implementation backed by the IDM database.
 *
 * Strategy:
 * - Resolve ApplicationScope by (applicationKey, stageKey)
 * - Resolve Role entities by (scopeId, roleName)
 * - Resolve RolePermissionAssignments and return raw permission names
 *
 * Notes:
 * - This class intentionally lives in the IDM domain layer
 * - Normalization (trim / distinct / sort) is handled by the generic PermissionResolver
 */
@Service
@RequiredArgsConstructor
public class IdmDatabasePermissionAuthoritySource implements PermissionAuthoritySource {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final RoleEntityService roleEntityService;
    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

    @Override
    @Transactional(readOnly = true)
    public List<String> loadPermissionNames(String applicationKey, String stageKey, List<String> roleNames) {

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
                .toList();
    }
}