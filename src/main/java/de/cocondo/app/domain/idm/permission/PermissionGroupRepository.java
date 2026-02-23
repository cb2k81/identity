package de.cocondo.app.domain.idm.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for PermissionGroup.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, String> {

    Optional<PermissionGroup> findByApplicationScope_IdAndName(String applicationScopeId, String name);
}