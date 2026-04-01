package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_SCOPE_ASSIGN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignApplicationScopeToUserHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserApplicationScopeAssignmentEntityService assignmentService;

    @PreAuthorize("hasAuthority('" + IDM_USER_SCOPE_ASSIGN + "')")
    public void handle(AssignApplicationScopeToUserRequestDTO request) {

        if (request.getUserAccountId() == null || request.getUserAccountId().isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        if (request.getApplicationScopeId() == null || request.getApplicationScopeId().isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        UserAccount user = userAccountEntityService.loadById(request.getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("UserAccount not found"));

        ApplicationScope scope = applicationScopeEntityService.loadById(request.getApplicationScopeId())
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        if (assignmentService.existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "ApplicationScope is already assigned to user"
            );
        }

        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(scope);

        assignmentService.save(assignment);

        log.info("ApplicationScope assigned to user: userId={}, scopeId={}, applicationKey={}, stageKey={}",
                user.getId(), scope.getId(), scope.getApplicationKey(), scope.getStageKey());
    }
}