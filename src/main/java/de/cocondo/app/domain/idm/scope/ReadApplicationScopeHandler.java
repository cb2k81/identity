package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadApplicationScopeHandler {

    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public ApplicationScopeDTO handle(String scopeId) {

        if (scopeId == null || scopeId.isBlank()) {
            throw new IllegalArgumentException("scopeId must not be blank");
        }

        ApplicationScope scope = scopeEntityService.loadById(scopeId)
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(scope.getId());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setDescription(scope.getDescription());

        return dto;
    }
}