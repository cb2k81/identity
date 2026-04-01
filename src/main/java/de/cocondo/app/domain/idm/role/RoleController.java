package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.role.dto.UpdateRoleRequestDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Role.
 *
 * Architecture:
 * Controller -> DomainService -> Handler -> EntityService -> Repository
 */
@RestController
@RequestMapping("/api/idm/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleManagementDomainService roleManagementDomainService;

    @GetMapping("/list")
    public PagedResponseDTO<RoleDTO> listPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "applicationScopeId", required = false) String applicationScopeId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "systemProtected", required = false) Boolean systemProtected
    ) {
        return roleManagementDomainService.listRolesPaged(
                page,
                size,
                sortBy,
                sortDir,
                applicationScopeId,
                name,
                systemProtected
        );
    }

    @GetMapping
    public List<RoleDTO> list() {
        return roleManagementDomainService.listRoles();
    }

    @GetMapping("/{id}")
    public RoleDTO read(@PathVariable("id") String id) {
        return roleManagementDomainService.readRole(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDTO create(@RequestBody CreateRoleRequestDTO request) {
        return roleManagementDomainService.createRole(request);
    }

    @PutMapping("/{id}")
    public RoleDTO update(@PathVariable("id") String id,
                          @RequestBody UpdateRoleRequestDTO request) {
        return roleManagementDomainService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        roleManagementDomainService.deleteRole(id);
    }
}