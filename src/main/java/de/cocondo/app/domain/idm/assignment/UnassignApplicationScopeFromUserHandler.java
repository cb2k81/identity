package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_SCOPE_UNASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnassignApplicationScopeFromUserHandler {

    private final UserApplicationScopeAssignmentEntityService assignmentService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_USER_SCOPE_UNASSIGN + "')")
    public void handle(AssignApplicationScopeToUserRequestDTO request) {

        if (request.getUserAccountId() == null || request.getUserAccountId().isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        if (request.getApplicationScopeId() == null || request.getApplicationScopeId().isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        boolean hasRolesInScope = userRoleAssignmentEntityService.loadAllByUserAccountId(request.getUserAccountId())
                .stream()
                .anyMatch(a -> a.getRole().getApplicationScope().getId().equals(request.getApplicationScopeId()));

        if (hasRolesInScope) {
            throw new IllegalArgumentException(
                    "ApplicationScope cannot be unassigned while user still has roles in this scope"
            );
        }

        assignmentService.loadByUserAccountIdAndApplicationScopeId(
                        request.getUserAccountId(),
                        request.getApplicationScopeId()
                )
                .ifPresent(assignment -> {
                    assignmentService.delete(assignment);
                    log.info("ApplicationScope unassigned from user: userId={}, scopeId={}",
                            request.getUserAccountId(), request.getApplicationScopeId());
                });
    }
}