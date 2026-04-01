package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.role.dto.UpdateRoleRequestDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade for role management use cases.
 *
 * Controllers interact only with this service.
 */
@Service
@RequiredArgsConstructor
public class RoleManagementDomainService {

    private final CreateRoleHandler createRoleHandler;
    private final ListRolesHandler listRolesHandler;
    private final ListRolesPagedHandler listRolesPagedHandler;
    private final ReadRoleHandler readRoleHandler;
    private final UpdateRoleHandler updateRoleHandler;
    private final DeleteRoleHandler deleteRoleHandler;

    public RoleDTO createRole(CreateRoleRequestDTO request) {
        return createRoleHandler.handle(request);
    }

    public List<RoleDTO> listRoles() {
        return listRolesHandler.handle();
    }

    public PagedResponseDTO<RoleDTO> listRolesPaged(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String applicationScopeId,
            String name,
            Boolean systemProtected
    ) {
        return listRolesPagedHandler.handle(page, size, sortBy, sortDir, applicationScopeId, name, systemProtected);
    }

    public RoleDTO readRole(String roleId) {
        return readRoleHandler.handle(roleId);
    }

    public RoleDTO updateRole(String roleId, UpdateRoleRequestDTO request) {
        return updateRoleHandler.handle(roleId, request);
    }

    public void deleteRole(String roleId) {
        deleteRoleHandler.handle(roleId);
    }
}