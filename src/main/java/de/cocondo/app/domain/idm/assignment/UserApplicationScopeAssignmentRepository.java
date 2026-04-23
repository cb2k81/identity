package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.user.UserAccountState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserApplicationScopeAssignment.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface UserApplicationScopeAssignmentRepository extends JpaRepository<UserApplicationScopeAssignment, String> {

    Optional<UserApplicationScopeAssignment> findByUserAccount_IdAndApplicationScope_Id(
            String userAccountId,
            String applicationScopeId
    );

    List<UserApplicationScopeAssignment> findAllByUserAccount_Id(String userAccountId);

    List<UserApplicationScopeAssignment> findAllByApplicationScope_Id(String applicationScopeId);

    Page<UserApplicationScopeAssignment> findAllByUserAccount_Id(String userAccountId, Pageable pageable);

    Page<UserApplicationScopeAssignment> findAllByApplicationScope_Id(String applicationScopeId, Pageable pageable);

    @Query(
            value = """
                    select assignment
                    from UserApplicationScopeAssignment assignment
                    where assignment.applicationScope.id = :applicationScopeId
                      and (:username is null or lower(assignment.userAccount.username) like lower(concat('%', :username, '%')))
                      and (:displayName is null or lower(assignment.userAccount.displayName) like lower(concat('%', :displayName, '%')))
                      and (:email is null or lower(assignment.userAccount.email) like lower(concat('%', :email, '%')))
                      and (:state is null or assignment.userAccount.state = :state)
                    """,
            countQuery = """
                    select count(assignment)
                    from UserApplicationScopeAssignment assignment
                    where assignment.applicationScope.id = :applicationScopeId
                      and (:username is null or lower(assignment.userAccount.username) like lower(concat('%', :username, '%')))
                      and (:displayName is null or lower(assignment.userAccount.displayName) like lower(concat('%', :displayName, '%')))
                      and (:email is null or lower(assignment.userAccount.email) like lower(concat('%', :email, '%')))
                      and (:state is null or assignment.userAccount.state = :state)
                    """
    )
    Page<UserApplicationScopeAssignment> findPageByApplicationScopeIdAndUserFilters(
            @Param("applicationScopeId") String applicationScopeId,
            @Param("username") String username,
            @Param("displayName") String displayName,
            @Param("email") String email,
            @Param("state") UserAccountState state,
            Pageable pageable
    );

    boolean existsByUserAccount_IdAndApplicationScope_Id(String userAccountId, String applicationScopeId);

    boolean existsByApplicationScope_Id(String applicationScopeId);
}