package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_ROLE_UNASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnassignRoleFromUserHandler {

    private final UserRoleAssignmentEntityService assignmentService;

    @PreAuthorize("hasAuthority('" + IDM_USER_ROLE_UNASSIGN + "')")
    public void handle(AssignRoleToUserRequestDTO request) {

        if (request.getUserAccountId() == null || request.getUserAccountId().isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        if (request.getRoleId() == null || request.getRoleId().isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        assignmentService.loadAllByUserAccountId(request.getUserAccountId())
                .stream()
                .filter(a -> a.getRole().getId().equals(request.getRoleId()))
                .forEach(a -> {
                    assignmentService.delete(a);
                    log.info("Role unassigned from user: userId={}, roleId={}",
                            request.getUserAccountId(), request.getRoleId());
                });
    }
}