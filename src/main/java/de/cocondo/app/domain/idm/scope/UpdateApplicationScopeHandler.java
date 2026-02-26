package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.config.IdmManagementAuthorities;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.UpdateApplicationScopeRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateApplicationScopeHandler {

    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IdmManagementAuthorities.IDM_SCOPE_UPDATE + "')")
    public ApplicationScopeDTO handle(String scopeId, UpdateApplicationScopeRequestDTO request) {

        if (scopeId == null || scopeId.isBlank()) {
            throw new IllegalArgumentException("scopeId must not be blank");
        }

        ApplicationScope scope = scopeEntityService.loadById(scopeId)
                .orElseThrow(() -> new IllegalArgumentException("ApplicationScope not found"));

        scope.setDescription(request.getDescription());

        ApplicationScope saved = scopeEntityService.save(scope);

        log.info("ApplicationScope updated: id={}", saved.getId());

        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(saved.getId());
        dto.setApplicationKey(saved.getApplicationKey());
        dto.setStageKey(saved.getStageKey());
        dto.setDescription(saved.getDescription());

        return dto;
    }
}