package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idm/assignments")
@RequiredArgsConstructor
public class UserApplicationScopeAssignmentController {

    private final AssignApplicationScopeToUserHandler assignApplicationScopeToUserHandler;
    private final UnassignApplicationScopeFromUserHandler unassignApplicationScopeFromUserHandler;

    @PostMapping("/user-scope")
    public void assign(@RequestBody AssignApplicationScopeToUserRequestDTO request) {
        assignApplicationScopeToUserHandler.handle(request);
    }

    @DeleteMapping("/user-scope")
    public void unassign(@RequestBody AssignApplicationScopeToUserRequestDTO request) {
        unassignApplicationScopeFromUserHandler.handle(request);
    }
}