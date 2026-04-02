package de.cocondo.app.domain.idm.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for RolePermissionAssignment.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface RolePermissionAssignmentRepository extends JpaRepository<RolePermissionAssignment, String> {

    List<RolePermissionAssignment> findAllByRole_Id(String roleId);

    List<RolePermissionAssignment> findAllByPermission_Id(String permissionId);

    Page<RolePermissionAssignment> findAllByRole_Id(String roleId, Pageable pageable);

    Page<RolePermissionAssignment> findAllByPermission_Id(String permissionId, Pageable pageable);

    boolean existsByRole_IdAndPermission_Id(String roleId, String permissionId);
}