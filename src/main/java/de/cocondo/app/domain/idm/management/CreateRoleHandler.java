package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.management.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.management.dto.RoleDTO;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_ROLE_CREATE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateRoleHandler {

    private final RoleEntityService roleEntityService;
    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_CREATE + "')")
    public RoleDTO handle(CreateRoleRequestDTO request) {

        if (request.getApplicationScopeId() == null || request.getApplicationScopeId().isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        ApplicationScope scope = scopeEntityService.loadById(request.getApplicationScopeId())
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        roleEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), request.getName())
                .ifPresent(r -> {
                    throw new IllegalArgumentException("Role already exists in this scope");
                });

        Role role = new Role();
        role.setApplicationScope(scope);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSystemProtected(request.isSystemProtected());

        Role saved = roleEntityService.save(role);

        log.info("Role created: id={}, scopeId={}, name={}",
                saved.getId(), scope.getId(), saved.getName());

        RoleDTO dto = new RoleDTO();
        dto.setId(saved.getId());
        dto.setApplicationScopeId(scope.getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setSystemProtected(saved.isSystemProtected());

        return dto;
    }
}