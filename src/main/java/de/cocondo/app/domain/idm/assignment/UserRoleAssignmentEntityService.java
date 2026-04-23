package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.user.UserAccountState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public List<UserRoleAssignment> loadAllByUserAccountIdAndScope(
            String userAccountId,
            String applicationKey,
            String stageKey
    ) {
        return repository.findAllByUserAccount_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
                userAccountId,
                applicationKey,
                stageKey
        );
    }

    public List<UserRoleAssignment> loadAllByRoleIdAndScope(
            String roleId,
            String applicationKey,
            String stageKey
    ) {
        return repository.findAllByRole_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
                roleId,
                applicationKey,
                stageKey
        );
    }

    public Page<UserRoleAssignment> loadPageByUserAccountIdAndScope(
            String userAccountId,
            String applicationKey,
            String stageKey,
            Pageable pageable
    ) {
        return repository.findAllByUserAccount_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
                userAccountId,
                applicationKey,
                stageKey,
                pageable
        );
    }

    public Page<UserRoleAssignment> loadPageByRoleIdAndScope(
            String roleId,
            String applicationKey,
            String stageKey,
            Pageable pageable
    ) {
        return repository.findAllByRole_IdAndRole_ApplicationScope_ApplicationKeyAndRole_ApplicationScope_StageKey(
                roleId,
                applicationKey,
                stageKey,
                pageable
        );
    }

    public Page<UserRoleAssignment> loadPageByRoleIdAndScope(
            String roleId,
            String applicationKey,
            String stageKey,
            String username,
            String displayName,
            String email,
            UserAccountState state,
            Pageable pageable
    ) {
        return repository.findPageByRoleIdAndScopeAndUserFilters(
                roleId,
                applicationKey,
                stageKey,
                username,
                displayName,
                email,
                state,
                pageable
        );
    }

    public boolean existsByUserAccountIdAndRoleId(String userAccountId, String roleId) {
        return repository.existsByUserAccount_IdAndRole_Id(userAccountId, roleId);
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