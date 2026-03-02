package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idm/roles")
@RequiredArgsConstructor
public class RoleController {

    private final CreateRoleHandler createRoleHandler;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDTO create(@Valid @RequestBody CreateRoleRequestDTO request) {
        return createRoleHandler.handle(request);
    }
}