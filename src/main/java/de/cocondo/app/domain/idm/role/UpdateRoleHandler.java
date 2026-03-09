package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.role.dto.UpdateRoleRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_UPDATE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateRoleHandler {

    private final RoleEntityService roleEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_UPDATE + "')")
    public RoleDTO handle(String roleId, UpdateRoleRequestDTO request) {

        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        role.setDescription(request.getDescription());

        Role saved = roleEntityService.save(role);

        log.info("Role updated: roleId={}", roleId);

        RoleDTO dto = new RoleDTO();
        dto.setId(saved.getId());
        dto.setApplicationScopeId(saved.getApplicationScope().getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setSystemProtected(saved.isSystemProtected());

        return dto;
    }
}