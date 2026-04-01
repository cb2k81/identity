package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.permission.PermissionManagementDomainService;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.CreateApplicationScopeRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.UpdateApplicationScopeRequestDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for ApplicationScope.
 *
 * Architecture:
 * Controller -> DomainService/Facade -> UseCase Handler -> EntityService -> Repository
 *
 * No direct persistence access from controllers.
 * No @PreAuthorize in controllers (security is enforced in handlers/domain services).
 */
@RestController
@RequestMapping("/api/idm/scopes")
@RequiredArgsConstructor
public class ApplicationScopeController {

    private final PermissionManagementDomainService permissionManagementDomainService;

    @GetMapping("/list")
    public PagedResponseDTO<ApplicationScopeDTO> listPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "applicationKey") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "applicationKey", required = false) String applicationKey,
            @RequestParam(name = "stageKey", required = false) String stageKey,
            @RequestParam(name = "description", required = false) String description
    ) {
        return permissionManagementDomainService.listApplicationScopesPaged(
                page,
                size,
                sortBy,
                sortDir,
                applicationKey,
                stageKey,
                description
        );
    }

    @GetMapping
    public List<ApplicationScopeDTO> list() {
        return permissionManagementDomainService.listApplicationScopes();
    }

    @GetMapping("/{id}")
    public ApplicationScopeDTO read(@PathVariable("id") String id) {
        return permissionManagementDomainService.readApplicationScope(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationScopeDTO create(@RequestBody CreateApplicationScopeRequestDTO request) {
        return permissionManagementDomainService.createApplicationScope(request);
    }

    @PutMapping("/{id}")
    public ApplicationScopeDTO update(@PathVariable("id") String id,
                                      @RequestBody UpdateApplicationScopeRequestDTO request) {
        return permissionManagementDomainService.updateApplicationScope(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        permissionManagementDomainService.deleteApplicationScope(id);
    }
}