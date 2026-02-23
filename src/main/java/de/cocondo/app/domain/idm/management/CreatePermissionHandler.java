package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.management.dto.CreatePermissionRequestDTO;
import de.cocondo.app.domain.idm.management.dto.PermissionDTO;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroup;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_PERMISSION_CREATE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreatePermissionHandler {

    private final PermissionEntityService permissionEntityService;
    private final PermissionGroupEntityService permissionGroupEntityService;
    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_PERMISSION_CREATE + "')")
    public PermissionDTO handle(CreatePermissionRequestDTO request) {

        ApplicationScope scope = scopeEntityService.loadById(request.getApplicationScopeId())
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        PermissionGroup group = permissionGroupEntityService.loadById(request.getPermissionGroupId())
                .orElseThrow(() -> new IllegalArgumentException("PermissionGroup not found"));

        if (!group.getApplicationScope().getId().equals(scope.getId())) {
            log.warn("Scope violation between Permission and PermissionGroup");
            throw new IllegalArgumentException("PermissionGroup belongs to different scope");
        }

        permissionEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), request.getName())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Permission already exists in this scope");
                });

        Permission permission = new Permission();
        permission.setApplicationScope(scope);
        permission.setPermissionGroup(group);
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setSystemProtected(request.isSystemProtected());

        Permission saved = permissionEntityService.save(permission);

        log.info("Permission created: id={}, scopeId={}, name={}",
                saved.getId(), scope.getId(), saved.getName());

        PermissionDTO dto = new PermissionDTO();
        dto.setId(saved.getId());
        dto.setApplicationScopeId(scope.getId());
        dto.setPermissionGroupId(group.getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setSystemProtected(saved.isSystemProtected());

        return dto;
    }
}