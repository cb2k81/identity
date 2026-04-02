package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_PERMISSION_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesOfPermissionHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_PERMISSION_READ + "')")
    public List<RoleDTO> handle(String permissionId) {

        if (permissionId == null || permissionId.isBlank()) {
            throw new IllegalArgumentException("permissionId must not be blank");
        }

        return rolePermissionAssignmentEntityService
                .loadAllByPermissionId(permissionId)
                .stream()
                .map(RolePermissionAssignment::getRole)
                .filter(role -> role != null)
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