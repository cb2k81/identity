package de.cocondo.app.domain.idm.permission;

import de.cocondo.app.domain.idm.assignment.AssignPermissionToRoleHandler;
import de.cocondo.app.domain.idm.assignment.AssignRoleToUserHandler;
import de.cocondo.app.domain.idm.assignment.UnassignPermissionFromRoleHandler;
import de.cocondo.app.domain.idm.assignment.UnassignRoleFromUserHandler;
import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.CreatePermissionGroupRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.CreatePermissionRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import de.cocondo.app.domain.idm.permission.dto.PermissionGroupDTO;
import de.cocondo.app.domain.idm.role.CreateRoleHandler;
import de.cocondo.app.domain.idm.role.DeleteRoleHandler;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.scope.CreateApplicationScopeHandler;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.CreateApplicationScopeRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade for IDM permission and role management.
 *
 * This class delegates to dedicated use-case handlers
 * to avoid becoming a God-Service.
 */
@Service
@RequiredArgsConstructor
public class PermissionManagementDomainService {

    private final CreateApplicationScopeHandler createApplicationScopeHandler;
    private final CreatePermissionGroupHandler createPermissionGroupHandler;
    private final CreatePermissionHandler createPermissionHandler;
    private final CreateRoleHandler createRoleHandler;
    private final AssignPermissionToRoleHandler assignPermissionToRoleHandler;
    private final AssignRoleToUserHandler assignRoleToUserHandler;
    private final DeleteRoleHandler deleteRoleHandler;
    private final DeletePermissionHandler deletePermissionHandler;
    private final UnassignPermissionFromRoleHandler unassignPermissionFromRoleHandler;
    private final UnassignRoleFromUserHandler unassignRoleFromUserHandler;

    public ApplicationScopeDTO createApplicationScope(CreateApplicationScopeRequestDTO request) {
        return createApplicationScopeHandler.handle(request);
    }

    public PermissionGroupDTO createPermissionGroup(CreatePermissionGroupRequestDTO request) {
        return createPermissionGroupHandler.handle(request);
    }

    public PermissionDTO createPermission(CreatePermissionRequestDTO request) {
        return createPermissionHandler.handle(request);
    }

    public RoleDTO createRole(CreateRoleRequestDTO request) {
        return createRoleHandler.handle(request);
    }

    public void assignPermissionToRole(AssignPermissionToRoleRequestDTO request) {
        assignPermissionToRoleHandler.handle(request);
    }

    public void assignRoleToUser(AssignRoleToUserRequestDTO request) {
        assignRoleToUserHandler.handle(request);
    }

    public void unassignPermissionFromRole(AssignPermissionToRoleRequestDTO request) {
        unassignPermissionFromRoleHandler.handle(request);
    }

    public void unassignRoleFromUser(AssignRoleToUserRequestDTO request) {
        unassignRoleFromUserHandler.handle(request);
    }

    public void deleteRole(String roleId) {
        deleteRoleHandler.handle(roleId);
    }

    public void deletePermission(String permissionId) {
        deletePermissionHandler.handle(permissionId);
    }
}