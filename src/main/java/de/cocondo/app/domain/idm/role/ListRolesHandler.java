package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesHandler {

    private final RoleRepository roleRepository;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public List<RoleDTO> handle() {

        return roleRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private RoleDTO toDto(Role role) {

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setApplicationScopeId(role.getApplicationScope().getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemProtected(role.isSystemProtected());

        return dto;
    }
}