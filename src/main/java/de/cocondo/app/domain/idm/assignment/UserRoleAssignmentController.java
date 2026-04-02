package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/idm/assignments")
@RequiredArgsConstructor
public class UserRoleAssignmentController {

    private final AssignRoleToUserHandler assignRoleToUserHandler;
    private final UnassignRoleFromUserHandler unassignRoleFromUserHandler;
    private final ListRolesOfUserInScopeHandler listRolesOfUserInScopeHandler;
    private final ListUsersOfRoleInScopeHandler listUsersOfRoleInScopeHandler;
    private final ListRolesOfUserInScopePagedHandler listRolesOfUserInScopePagedHandler;
    private final ListUsersOfRoleInScopePagedHandler listUsersOfRoleInScopePagedHandler;

    @PostMapping("/user-role")
    public void assign(@RequestBody AssignRoleToUserRequestDTO request) {
        assignRoleToUserHandler.handle(request);
    }

    @DeleteMapping("/user-role")
    public void unassign(@RequestBody AssignRoleToUserRequestDTO request) {
        unassignRoleFromUserHandler.handle(request);
    }

    @GetMapping("/user-role/users/{userAccountId}/roles")
    public List<RoleDTO> listRolesOfUserInScope(
            @PathVariable("userAccountId") String userAccountId,
            @RequestParam("applicationKey") String applicationKey,
            @RequestParam("stageKey") String stageKey
    ) {
        return listRolesOfUserInScopeHandler.handle(userAccountId, applicationKey, stageKey);
    }

    @GetMapping("/user-role/roles/{roleId}/users")
    public List<UserAccountDTO> listUsersOfRoleInScope(
            @PathVariable("roleId") String roleId,
            @RequestParam("applicationKey") String applicationKey,
            @RequestParam("stageKey") String stageKey
    ) {
        return listUsersOfRoleInScopeHandler.handle(roleId, applicationKey, stageKey);
    }

    @GetMapping("/user-role/users/{userAccountId}/roles/list")
    public PagedResponseDTO<RoleDTO> listRolesOfUserInScopePaged(
            @PathVariable("userAccountId") String userAccountId,
            @RequestParam("applicationKey") String applicationKey,
            @RequestParam("stageKey") String stageKey,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return listRolesOfUserInScopePagedHandler.handle(
                userAccountId,
                applicationKey,
                stageKey,
                page,
                size,
                sortBy,
                sortDir
        );
    }

    @GetMapping("/user-role/roles/{roleId}/users/list")
    public PagedResponseDTO<UserAccountDTO> listUsersOfRoleInScopePaged(
            @PathVariable("roleId") String roleId,
            @RequestParam("applicationKey") String applicationKey,
            @RequestParam("stageKey") String stageKey,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "username") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        return listUsersOfRoleInScopePagedHandler.handle(
                roleId,
                applicationKey,
                stageKey,
                page,
                size,
                sortBy,
                sortDir
        );
    }
}