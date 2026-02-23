package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.assignment.UserRoleAssignment;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.management.dto.AssignRoleToUserRequestDTO;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_USER_ROLE_ASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignRoleToUserHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final RoleEntityService roleEntityService;
    private final UserRoleAssignmentEntityService assignmentService;

    @PreAuthorize("hasAuthority('" + IDM_USER_ROLE_ASSIGN + "')")
    public void handle(AssignRoleToUserRequestDTO request) {

        if (request.getUserAccountId() == null || request.getUserAccountId().isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        if (request.getRoleId() == null || request.getRoleId().isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        UserAccount user = userAccountEntityService.loadById(request.getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("UserAccount not found"));

        Role role = roleEntityService.loadById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUserAccount(user);
        assignment.setRole(role);

        assignmentService.save(assignment);

        log.info("Role assigned to user: userId={}, roleId={}, scopeId={}",
                user.getId(), role.getId(), role.getApplicationScope().getId());
    }
}