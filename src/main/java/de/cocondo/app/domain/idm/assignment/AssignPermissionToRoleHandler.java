package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_PERMISSION_ASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignPermissionToRoleHandler {

    private final RoleEntityService roleEntityService;
    private final PermissionEntityService permissionEntityService;
    private final RolePermissionAssignmentEntityService assignmentService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_PERMISSION_ASSIGN + "')")
    public void handle(AssignPermissionToRoleRequestDTO request) {

        Role role = roleEntityService.loadById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Permission permission = permissionEntityService.loadById(request.getPermissionId())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        if (!role.getApplicationScope().getId()
                .equals(permission.getApplicationScope().getId())) {
            log.warn("Scope violation in RolePermissionAssignment");
            throw new IllegalArgumentException("Role and Permission belong to different scope");
        }

        RolePermissionAssignment assignment = new RolePermissionAssignment();
        assignment.setRole(role);
        assignment.setPermission(permission);

        assignmentService.save(assignment);

        log.info("Permission assigned: roleId={}, permissionId={}",
                role.getId(), permission.getId());
    }
}