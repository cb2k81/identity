package de.cocondo.app.domain.idm.permission;

import de.cocondo.app.domain.idm.scope.CreateApplicationScopeHandler;
import de.cocondo.app.domain.idm.scope.DeleteApplicationScopeHandler;
import de.cocondo.app.domain.idm.scope.ListApplicationScopesHandler;
import de.cocondo.app.domain.idm.scope.ListApplicationScopesPagedHandler;
import de.cocondo.app.domain.idm.scope.ReadApplicationScopeHandler;
import de.cocondo.app.domain.idm.scope.UpdateApplicationScopeHandler;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.CreateApplicationScopeRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.UpdateApplicationScopeRequestDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Permission management facade.
 *
 * Architecture:
 * Controller -> DomainService -> UseCase Handler -> EntityService -> Repository
 *
 * This service orchestrates permission related management operations.
 * Controllers must only depend on this service and never directly on handlers.
 */
@Service
@RequiredArgsConstructor
public class PermissionManagementDomainService {

    private final CreateApplicationScopeHandler createApplicationScopeHandler;
    private final ListApplicationScopesHandler listApplicationScopesHandler;
    private final ListApplicationScopesPagedHandler listApplicationScopesPagedHandler;
    private final ReadApplicationScopeHandler readApplicationScopeHandler;
    private final UpdateApplicationScopeHandler updateApplicationScopeHandler;
    private final DeleteApplicationScopeHandler deleteApplicationScopeHandler;

    /**
     * Creates a new ApplicationScope.
     */
    public ApplicationScopeDTO createApplicationScope(CreateApplicationScopeRequestDTO request) {
        return createApplicationScopeHandler.handle(request);
    }

    /**
     * Returns all ApplicationScopes.
     */
    public List<ApplicationScopeDTO> listApplicationScopes() {
        return listApplicationScopesHandler.handle();
    }

    /**
     * Returns paged ApplicationScopes for UI list usage.
     */
    public PagedResponseDTO<ApplicationScopeDTO> listApplicationScopesPaged(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String applicationKey,
            String stageKey,
            String description
    ) {
        return listApplicationScopesPagedHandler.handle(page, size, sortBy, sortDir, applicationKey, stageKey, description);
    }

    /**
     * Reads a specific ApplicationScope.
     */
    public ApplicationScopeDTO readApplicationScope(String scopeId) {
        return readApplicationScopeHandler.handle(scopeId);
    }

    /**
     * Updates an ApplicationScope.
     */
    public ApplicationScopeDTO updateApplicationScope(String scopeId, UpdateApplicationScopeRequestDTO request) {
        return updateApplicationScopeHandler.handle(scopeId, request);
    }

    /**
     * Deletes an ApplicationScope.
     */
    public void deleteApplicationScope(String scopeId) {
        deleteApplicationScopeHandler.handle(scopeId);
    }
}