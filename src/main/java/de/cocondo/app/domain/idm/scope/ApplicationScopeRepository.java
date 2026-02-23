package de.cocondo.app.domain.idm.scope;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for ApplicationScope.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface ApplicationScopeRepository extends JpaRepository<ApplicationScope, String> {

    Optional<ApplicationScope> findByApplicationKeyAndStageKey(String applicationKey, String stageKey);
}