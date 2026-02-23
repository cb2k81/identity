package de.cocondo.app.domain.idm.assignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Entity service for UserRoleAssignment.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
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

    public UserRoleAssignment save(UserRoleAssignment assignment) {
        return repository.save(assignment);
    }
}