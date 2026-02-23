package de.cocondo.app.domain.idm.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Entity service for UserRoleAssignment.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleAssignmentEntityService {

    private final UserRoleAssignmentRepository repository;

    public Optional<UserRoleAssignment> loadById(String id) {
        return repository.findById(id);
    }

    public List<UserRoleAssignment> loadAllByUserAccountId(String userAccountId) {
        return repository.findAllByUserAccount_Id(userAccountId);
    }

    public List<UserRoleAssignment> loadAllByRoleId(String roleId) {
        return repository.findAllByRole_Id(roleId);
    }

    @Transactional
    public UserRoleAssignment save(UserRoleAssignment assignment) {
        return repository.save(assignment);
    }

    @Transactional
    public void delete(UserRoleAssignment assignment) {
        repository.delete(assignment);
    }
}