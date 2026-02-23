package de.cocondo.app.domain.idm.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for UserAccount aggregate.
 *
 * Responsibilities:
 * - Persistence access only
 * - No business logic
 * - No security logic
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    /**
     * Loads a user account by its unique username.
     *
     * @param username unique login name
     * @return optional UserAccount
     */
    Optional<UserAccount> findByUsername(String username);
}