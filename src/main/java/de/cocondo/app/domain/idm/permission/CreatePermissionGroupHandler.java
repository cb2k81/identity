package de.cocondo.app.domain.idm.permission;

import de.cocondo.app.domain.idm.permission.dto.CreatePermissionGroupRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.PermissionGroupDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_PERMISSION_CREATE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreatePermissionGroupHandler {

    private final PermissionGroupEntityService permissionGroupEntityService;
    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_PERMISSION_CREATE + "')")
    public PermissionGroupDTO handle(CreatePermissionGroupRequestDTO request) {

        if (request.getApplicationScopeId() == null || request.getApplicationScopeId().isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        ApplicationScope scope = scopeEntityService.loadById(request.getApplicationScopeId())
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        permissionGroupEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), request.getName())
                .ifPresent(g -> {
                    throw new IllegalArgumentException("PermissionGroup already exists in this scope");
                });

        PermissionGroup group = new PermissionGroup();
        group.setApplicationScope(scope);
        group.setName(request.getName());
        group.setDescription(request.getDescription());

        PermissionGroup saved = permissionGroupEntityService.save(group);

        log.info("PermissionGroup created: id={}, scopeId={}, name={}",
                saved.getId(), scope.getId(), saved.getName());

        PermissionGroupDTO dto = new PermissionGroupDTO();
        dto.setId(saved.getId());
        dto.setApplicationScopeId(scope.getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());

        return dto;
    }
}