package de.cocondo.app.domain.idm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Entity service for the UserAccount aggregate.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 *
 * Responsibilities:
 * - loading UserAccount aggregates
 * - persisting UserAccount aggregates
 *
 * This service:
 * - knows no DTOs
 * - knows no security logic
 * - contains no use-case orchestration
 */
@Service
@RequiredArgsConstructor
public class UserAccountEntityService {

    private final UserAccountRepository userAccountRepository;

    /**
     * Loads a UserAccount by its technical identifier.
     *
     * @param id technical identifier
     * @return optional UserAccount
     */
    public Optional<UserAccount> loadById(String id) {
        return userAccountRepository.findById(id);
    }

    /**
     * Loads a UserAccount by its unique username.
     *
     * @param username unique login name
     * @return optional UserAccount
     */
    public Optional<UserAccount> loadByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }

    /**
     * Persists the given UserAccount aggregate.
     *
     * @param userAccount aggregate to persist
     * @return persisted aggregate
     */
    public UserAccount save(UserAccount userAccount) {
        return userAccountRepository.save(userAccount);
    }
}