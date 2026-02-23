package de.cocondo.app.domain.idm.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByApplicationScope_IdAndName(String applicationScopeId, String name);

    List<Permission> findAllByApplicationScope_Id(String applicationScopeId);
}