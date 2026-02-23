package de.cocondo.app.domain.idm.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for UserApplicationScopeAssignment.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface UserApplicationScopeAssignmentRepository
        extends JpaRepository<UserApplicationScopeAssignment, String> {

    Optional<UserApplicationScopeAssignment> findByUserAccount_IdAndApplicationScope_Id(
            String userAccountId,
            String applicationScopeId
    );

    boolean existsByUserAccount_IdAndApplicationScope_Id(String userAccountId, String applicationScopeId);
}