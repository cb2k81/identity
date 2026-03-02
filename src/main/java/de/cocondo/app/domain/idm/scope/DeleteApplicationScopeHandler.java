package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_DELETE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteApplicationScopeHandler {

    private final ApplicationScopeEntityService scopeEntityService;
    private final PermissionEntityService permissionEntityService;
    private final PermissionGroupEntityService permissionGroupEntityService;
    private final RoleEntityService roleEntityService;
    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_DELETE + "')")
    public void handle(String scopeId) {

        if (scopeId == null || scopeId.isBlank()) {
            throw new IllegalArgumentException("scopeId must not be blank");
        }

        ApplicationScope scope = scopeEntityService.loadById(scopeId)
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        if (permissionEntityService.existsByApplicationScopeId(scopeId)) {
            throw new IllegalStateException("ApplicationScope has permissions and cannot be deleted");
        }

        if (permissionGroupEntityService.existsByApplicationScopeId(scopeId)) {
            throw new IllegalStateException("ApplicationScope has permission groups and cannot be deleted");
        }

        if (roleEntityService.existsByApplicationScopeId(scopeId)) {
            throw new IllegalStateException("ApplicationScope has roles and cannot be deleted");
        }

        if (userApplicationScopeAssignmentEntityService.existsByApplicationScopeId(scopeId)) {
            throw new IllegalStateException("ApplicationScope is assigned to users and cannot be deleted");
        }

        scopeEntityService.delete(scope);

        log.info("ApplicationScope deleted: scopeId={}", scopeId);
    }
}