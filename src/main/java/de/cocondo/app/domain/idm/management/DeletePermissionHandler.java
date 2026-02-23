package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_PERMISSION_DELETE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeletePermissionHandler {

    private final PermissionEntityService permissionEntityService;
    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_PERMISSION_DELETE + "')")
    public void handle(String permissionId) {

        if (permissionId == null || permissionId.isBlank()) {
            throw new IllegalArgumentException("permissionId must not be blank");
        }

        Permission permission = permissionEntityService.loadById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        if (permission.isSystemProtected()) {
            log.warn("Attempt to delete systemProtected permission: permissionId={}", permissionId);
            throw new IllegalStateException("System protected permission cannot be deleted");
        }

        if (!rolePermissionAssignmentEntityService.loadAllByPermissionId(permissionId).isEmpty()) {
            throw new IllegalStateException("Permission is assigned to roles and cannot be deleted");
        }

        permissionEntityService.delete(permission);

        log.info("Permission deleted: permissionId={}", permissionId);
    }
}