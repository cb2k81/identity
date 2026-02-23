package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.management.dto.*;
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