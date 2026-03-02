package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApplicationScopesHandler {

    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public List<ApplicationScopeDTO> handle() {

        return scopeEntityService.loadAll().stream()
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