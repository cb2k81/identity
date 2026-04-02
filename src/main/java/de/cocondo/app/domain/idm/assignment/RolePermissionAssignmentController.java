package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
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
}