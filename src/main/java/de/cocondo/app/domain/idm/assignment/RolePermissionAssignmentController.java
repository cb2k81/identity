package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoint for assigning/unassigning Permissions to/from Roles.
 *
 * Option A endpoint architecture:
 * - DDD oriented controller located in the bounded context "assignment"
 * - Public API path: /api/idm/assignments/...
 *
 * Security is enforced in the use-case handlers via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/idm/assignments")
@RequiredArgsConstructor
public class RolePermissionAssignmentController {

    private final AssignPermissionToRoleHandler assignPermissionToRoleHandler;
    private final UnassignPermissionFromRoleHandler unassignPermissionFromRoleHandler;
    private final ListPermissionsOfRoleHandler listPermissionsOfRoleHandler;
    private final ListRolesOfPermissionHandler listRolesOfPermissionHandler;
    private final ListPermissionsOfRolePagedHandler listPermissionsOfRolePagedHandler;
    private final ListRolesOfPermissionPagedHandler listRolesOfPermissionPagedHandler;

    @PostMapping("/role-permission")
    public ResponseEntity<Void> assignPermissionToRole(@RequestBody AssignPermissionToRoleRequestDTO request) {
        assignPermissionToRoleHandler.handle(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/role-permission")
    public ResponseEntity<Void> unassignPermissionFromRole(@RequestBody AssignPermissionToRoleRequestDTO request) {
        unassignPermissionFromRoleHandler.handle(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role-permission/roles/{roleId}/permissions")
    public List<PermissionDTO> listPermissionsOfRole(@PathVariable("roleId") String roleId) {
        return listPermissionsOfRoleHandler.handle(roleId);
    }

    @GetMapping("/role-permission/permissions/{permissionId}/roles")
    public List<RoleDTO> listRolesOfPermission(@PathVariable("permissionId") String permissionId) {
        return listRolesOfPermissionHandler.handle(permissionId);
    }

    @GetMapping("/role-permission/roles/{roleId}/permissions/list")
    public PagedResponseDTO<PermissionDTO> listPermissionsOfRolePaged(
            @PathVariable("roleId") String roleId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return listPermissionsOfRolePagedHandler.handle(roleId, page, size, sortBy, sortDir);
    }

    @GetMapping("/role-permission/permissions/{permissionId}/roles/list")
    public PagedResponseDTO<RoleDTO> listRolesOfPermissionPaged(
            @PathVariable("permissionId") String permissionId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return listRolesOfPermissionPagedHandler.handle(permissionId, page, size, sortBy, sortDir);
    }
}