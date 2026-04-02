package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListPermissionsOfRoleHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public List<PermissionDTO> handle(String roleId) {

        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        return rolePermissionAssignmentEntityService
                .loadAllByRoleId(roleId)
                .stream()
                .map(RolePermissionAssignment::getPermission)
                .filter(permission -> permission != null)
                .map(permission -> {
                    PermissionDTO dto = new PermissionDTO();
                    dto.setId(permission.getId());
                    dto.setApplicationScopeId(permission.getApplicationScope().getId());
                    dto.setPermissionGroupId(permission.getPermissionGroup().getId());
                    dto.setName(permission.getName());
                    dto.setDescription(permission.getDescription());
                    dto.setSystemProtected(permission.isSystemProtected());
                    return dto;
                })
                .toList();
    }
}