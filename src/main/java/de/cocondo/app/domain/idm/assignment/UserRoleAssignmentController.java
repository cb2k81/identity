package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idm/assignments/user-role")
@RequiredArgsConstructor
public class UserRoleAssignmentController {

    private final AssignRoleToUserHandler assignRoleToUserHandler;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void assign(@Valid @RequestBody AssignRoleToUserRequestDTO request) {
        assignRoleToUserHandler.handle(request);
    }
}