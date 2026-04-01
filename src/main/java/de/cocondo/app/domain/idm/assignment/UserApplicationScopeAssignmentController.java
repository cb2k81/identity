package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/idm/assignments")
@RequiredArgsConstructor
public class UserApplicationScopeAssignmentController {

    private final AssignApplicationScopeToUserHandler assignApplicationScopeToUserHandler;
    private final UnassignApplicationScopeFromUserHandler unassignApplicationScopeFromUserHandler;
    private final ListApplicationScopesOfUserHandler listApplicationScopesOfUserHandler;
    private final ListUsersOfApplicationScopeHandler listUsersOfApplicationScopeHandler;

    @PostMapping("/user-scope")
    public void assign(@RequestBody AssignApplicationScopeToUserRequestDTO request) {
        assignApplicationScopeToUserHandler.handle(request);
    }

    @DeleteMapping("/user-scope")
    public void unassign(@RequestBody AssignApplicationScopeToUserRequestDTO request) {
        unassignApplicationScopeFromUserHandler.handle(request);
    }

    @GetMapping("/user-scope/users/{userAccountId}/scopes")
    public List<ApplicationScopeDTO> listApplicationScopesOfUser(
            @PathVariable("userAccountId") String userAccountId
    ) {
        return listApplicationScopesOfUserHandler.handle(userAccountId);
    }

    @GetMapping("/user-scope/scopes/{applicationScopeId}/users")
    public List<UserAccountDTO> listUsersOfApplicationScope(
            @PathVariable("applicationScopeId") String applicationScopeId
    ) {
        return listUsersOfApplicationScopeHandler.handle(applicationScopeId);
    }
}