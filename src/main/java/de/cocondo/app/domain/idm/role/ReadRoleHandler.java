package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadRoleHandler {

    private final RoleEntityService roleEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public RoleDTO handle(String roleId) {

        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setApplicationScopeId(role.getApplicationScope().getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemProtected(role.isSystemProtected());

        return dto;
    }
}