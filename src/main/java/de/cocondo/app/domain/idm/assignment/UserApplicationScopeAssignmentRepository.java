package de.cocondo.app.domain.idm.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserApplicationScopeAssignment.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface UserApplicationScopeAssignmentRepository extends JpaRepository<UserApplicationScopeAssignment, String> {

    Optional<UserApplicationScopeAssignment> findByUserAccount_IdAndApplicationScope_Id(
            String userAccountId,
            String applicationScopeId
    );

    List<UserApplicationScopeAssignment> findAllByUserAccount_Id(String userAccountId);

    List<UserApplicationScopeAssignment> findAllByApplicationScope_Id(String applicationScopeId);

    Page<UserApplicationScopeAssignment> findAllByUserAccount_Id(String userAccountId, Pageable pageable);

    Page<UserApplicationScopeAssignment> findAllByApplicationScope_Id(String applicationScopeId, Pageable pageable);

    boolean existsByUserAccount_IdAndApplicationScope_Id(String userAccountId, String applicationScopeId);

    boolean existsByApplicationScope_Id(String applicationScopeId);
}