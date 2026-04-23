package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
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
    private final ListApplicationScopesOfUserPagedHandler listApplicationScopesOfUserPagedHandler;
    private final ListUsersOfApplicationScopePagedHandler listUsersOfApplicationScopePagedHandler;

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

    @GetMapping("/user-scope/users/{userAccountId}/scopes/list")
    public PagedResponseDTO<ApplicationScopeDTO> listApplicationScopesOfUserPaged(
            @PathVariable("userAccountId") String userAccountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "applicationKey") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return listApplicationScopesOfUserPagedHandler.handle(userAccountId, page, size, sortBy, sortDir);
    }

    @GetMapping("/user-scope/scopes/{applicationScopeId}/users/list")
    public PagedResponseDTO<UserAccountDTO> listUsersOfApplicationScopePaged(
            @PathVariable("applicationScopeId") String applicationScopeId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "username") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "displayName", required = false) String displayName,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "state", required = false) String state
    ) {
        return listUsersOfApplicationScopePagedHandler.handle(
                applicationScopeId,
                page,
                size,
                sortBy,
                sortDir,
                username,
                displayName,
                email,
                state
        );
    }
}