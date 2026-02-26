package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_DELETE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteRoleHandler {

    private final RoleEntityService roleEntityService;
    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_DELETE + "')")
    public void handle(String roleId) {

        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (role.isSystemProtected()) {
            log.warn("Attempt to delete systemProtected role: roleId={}", roleId);
            throw new IllegalStateException("System protected role cannot be deleted");
        }

        if (!rolePermissionAssignmentEntityService.loadAllByRoleId(roleId).isEmpty()) {
            throw new IllegalStateException("Role has assigned permissions and cannot be deleted");
        }

        if (!userRoleAssignmentEntityService.loadAllByRoleId(roleId).isEmpty()) {
            throw new IllegalStateException("Role is assigned to users and cannot be deleted");
        }

        roleEntityService.delete(role);

        log.info("Role deleted: roleId={}", roleId);
    }
}