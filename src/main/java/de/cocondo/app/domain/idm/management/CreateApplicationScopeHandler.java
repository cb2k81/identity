package de.cocondo.app.domain.idm.management;

import de.cocondo.app.domain.idm.management.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.management.dto.CreateApplicationScopeRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_SCOPE_CREATE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateApplicationScopeHandler {

    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_CREATE + "')")
    public ApplicationScopeDTO handle(CreateApplicationScopeRequestDTO request) {

        if (request.getApplicationKey() == null || request.getApplicationKey().isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (request.getStageKey() == null || request.getStageKey().isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        scopeEntityService
                .loadByApplicationKeyAndStageKey(request.getApplicationKey(), request.getStageKey())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("ApplicationScope already exists");
                });

        ApplicationScope scope = new ApplicationScope();
        scope.setApplicationKey(request.getApplicationKey());
        scope.setStageKey(request.getStageKey());
        scope.setDescription(request.getDescription());

        ApplicationScope saved = scopeEntityService.save(scope);

        log.info("ApplicationScope created: id={}, applicationKey={}, stageKey={}",
                saved.getId(), saved.getApplicationKey(), saved.getStageKey());

        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(saved.getId());
        dto.setApplicationKey(saved.getApplicationKey());
        dto.setStageKey(saved.getStageKey());
        dto.setDescription(saved.getDescription());

        return dto;
    }
}