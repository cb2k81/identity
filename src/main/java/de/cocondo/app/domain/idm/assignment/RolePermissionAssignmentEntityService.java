package de.cocondo.app.domain.idm.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Entity service for RolePermissionAssignment.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolePermissionAssignmentEntityService {

    private final RolePermissionAssignmentRepository repository;

    public Optional<RolePermissionAssignment> loadById(String id) {
        return repository.findById(id);
    }

    public List<RolePermissionAssignment> loadAllByRoleId(String roleId) {
        return repository.findAllByRole_Id(roleId);
    }

    @Transactional
    public RolePermissionAssignment save(RolePermissionAssignment assignment) {
        return repository.save(assignment);
    }

    @Transactional
    public void delete(RolePermissionAssignment assignment) {
        repository.delete(assignment);
    }

    public List<RolePermissionAssignment> loadAllByPermissionId(String permissionId) {
        return repository.findAllByPermission_Id(permissionId);
    }
}