package de.cocondo.app.domain.idm.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Entity service for UserApplicationScopeAssignment.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationScopeAssignmentEntityService {

    private final UserApplicationScopeAssignmentRepository repository;

    public Optional<UserApplicationScopeAssignment> loadByUserAccountIdAndApplicationScopeId(
            String userAccountId,
            String applicationScopeId
    ) {
        return repository.findByUserAccount_IdAndApplicationScope_Id(userAccountId, applicationScopeId);
    }

    public boolean existsByUserAccountIdAndApplicationScopeId(String userAccountId, String applicationScopeId) {
        return repository.existsByUserAccount_IdAndApplicationScope_Id(userAccountId, applicationScopeId);
    }

    @Transactional
    public UserApplicationScopeAssignment save(UserApplicationScopeAssignment assignment) {
        return repository.save(assignment);
    }
}