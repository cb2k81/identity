package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignment;
import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.management.dto.AssignPermissionToRoleRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_ROLE_PERMISSION_UNASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnassignPermissionFromRoleHandler {

    private final RolePermissionAssignmentEntityService assignmentService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_PERMISSION_UNASSIGN + "')")
    public void handle(AssignPermissionToRoleRequestDTO request) {

        if (request.getRoleId() == null || request.getRoleId().isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        if (request.getPermissionId() == null || request.getPermissionId().isBlank()) {
            throw new IllegalArgumentException("permissionId must not be blank");
        }

        assignmentService.loadAllByRoleId(request.getRoleId())
                .stream()
                .filter(a -> a.getPermission().getId().equals(request.getPermissionId()))
                .forEach(a -> {
                    assignmentService.delete(a);
                    log.info("Permission unassigned from role: roleId={}, permissionId={}",
                            request.getRoleId(), request.getPermissionId());
                });
    }
}