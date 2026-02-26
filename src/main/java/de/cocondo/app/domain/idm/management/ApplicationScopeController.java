package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.management.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_SCOPE_READ;

@RestController
@RequestMapping("/api/idm/scopes")
@RequiredArgsConstructor
public class ApplicationScopeController {

    private final ApplicationScopeEntityService applicationScopeEntityService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public List<ApplicationScopeDTO> list() {

        return applicationScopeEntityService.loadAll().stream()
                .map(this::toDto)
                .toList();
    }

    private ApplicationScopeDTO toDto(ApplicationScope scope) {

        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(scope.getId());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setDescription(scope.getDescription());

        return dto;
    }
}