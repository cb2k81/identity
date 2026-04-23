package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.user.UserAccountState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
            value = """
                    select assignment
                    from UserRoleAssignment assignment
                    where assignment.role.id = :roleId
                      and assignment.role.applicationScope.applicationKey = :applicationKey
                      and assignment.role.applicationScope.stageKey = :stageKey
                      and (:username is null or lower(assignment.userAccount.username) like lower(concat('%', :username, '%')))
                      and (:displayName is null or lower(assignment.userAccount.displayName) like lower(concat('%', :displayName, '%')))
                      and (:email is null or lower(assignment.userAccount.email) like lower(concat('%', :email, '%')))
                      and (:state is null or assignment.userAccount.state = :state)
                    """,
            countQuery = """
                    select count(assignment)
                    from UserRoleAssignment assignment
                    where assignment.role.id = :roleId
                      and assignment.role.applicationScope.applicationKey = :applicationKey
                      and assignment.role.applicationScope.stageKey = :stageKey
                      and (:username is null or lower(assignment.userAccount.username) like lower(concat('%', :username, '%')))
                      and (:displayName is null or lower(assignment.userAccount.displayName) like lower(concat('%', :displayName, '%')))
                      and (:email is null or lower(assignment.userAccount.email) like lower(concat('%', :email, '%')))
                      and (:state is null or assignment.userAccount.state = :state)
                    """
    )
    Page<UserRoleAssignment> findPageByRoleIdAndScopeAndUserFilters(
            @Param("roleId") String roleId,
            @Param("applicationKey") String applicationKey,
            @Param("stageKey") String stageKey,
            @Param("username") String username,
            @Param("displayName") String displayName,
            @Param("email") String email,
            @Param("state") UserAccountState state,
            Pageable pageable
    );

    boolean existsByUserAccount_IdAndRole_Id(String userAccountId, String roleId);
}