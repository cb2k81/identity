package de.cocondo.app.domain.idm.assignment;

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
}