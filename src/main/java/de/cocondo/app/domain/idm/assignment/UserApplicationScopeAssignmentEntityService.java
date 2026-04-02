package de.cocondo.app.domain.idm.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<UserApplicationScopeAssignment> loadAllByUserAccountId(String userAccountId) {
        return repository.findAllByUserAccount_Id(userAccountId);
    }

    public List<UserApplicationScopeAssignment> loadAllByApplicationScopeId(String applicationScopeId) {
        return repository.findAllByApplicationScope_Id(applicationScopeId);
    }

    public Page<UserApplicationScopeAssignment> loadPageByUserAccountId(String userAccountId, Pageable pageable) {
        return repository.findAllByUserAccount_Id(userAccountId, pageable);
    }

    public Page<UserApplicationScopeAssignment> loadPageByApplicationScopeId(String applicationScopeId, Pageable pageable) {
        return repository.findAllByApplicationScope_Id(applicationScopeId, pageable);
    }

    public boolean existsByUserAccountIdAndApplicationScopeId(String userAccountId, String applicationScopeId) {
        return repository.existsByUserAccount_IdAndApplicationScope_Id(userAccountId, applicationScopeId);
    }

    public boolean existsByApplicationScopeId(String applicationScopeId) {
        return repository.existsByApplicationScope_Id(applicationScopeId);
    }

    @Transactional
    public UserApplicationScopeAssignment save(UserApplicationScopeAssignment assignment) {
        return repository.save(assignment);
    }

    @Transactional
    public void delete(UserApplicationScopeAssignment assignment) {
        repository.delete(assignment);
    }
}