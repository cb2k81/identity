package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idm/assignments")
@RequiredArgsConstructor
public class UserRoleAssignmentController {

    private final AssignRoleToUserHandler assignRoleToUserHandler;
    private final UnassignRoleFromUserHandler unassignRoleFromUserHandler;

    @PostMapping("/user-role")
    public void assign(@RequestBody AssignRoleToUserRequestDTO request) {
        assignRoleToUserHandler.handle(request);
    }

    @DeleteMapping("/user-role")
    public void unassign(@RequestBody AssignRoleToUserRequestDTO request) {
        unassignRoleFromUserHandler.handle(request);
    }
}