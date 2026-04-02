package de.cocondo.app.domain.idm.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for UserRoleAssignment.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, String> {

    List<UserRoleAssignment> findAllByUserAccount_Id(String userAccountId);

    List<UserRoleAssignment> findAllByRole_Id(String roleId);

    List<UserRoleAssignment> findAllByUserAccount_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
            String userAccountId,
            String applicationKey,
            String stageKey
    );

    List<UserRoleAssignment> findAllByRole_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
            String roleId,
            String applicationKey,
            String stageKey
    );

    Page<UserRoleAssignment> findAllByUserAccount_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
            String userAccountId,
            String applicationKey,
            String stageKey,
            Pageable pageable
    );

    Page<UserRoleAssignment> findAllByRole_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
            String roleId,
            String applicationKey,
            String stageKey,
            Pageable pageable
    );

    boolean existsByUserAccount_IdAndRole_Id(String userAccountId, String roleId);
}