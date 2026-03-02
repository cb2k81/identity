package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}