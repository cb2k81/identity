package de.cocondo.app.domain.idm.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Role.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByApplicationScope_IdAndName(String applicationScopeId, String name);
}