package de.cocondo.app.domain.idm.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Entity service for ApplicationScope.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationScopeEntityService {

    private final ApplicationScopeRepository repository;

    public Optional<ApplicationScope> loadById(String id) {
        return repository.findById(id);
    }

    public Optional<ApplicationScope> loadByApplicationKeyAndStageKey(String applicationKey, String stageKey) {
        return repository.findByApplicationKeyAndStageKey(applicationKey, stageKey);
    }

    @Transactional
    public ApplicationScope save(ApplicationScope scope) {
        return repository.save(scope);
    }
}