package de.cocondo.app.domain.idm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// TODO-ARCH: Domain-Services dürfen nach außen nur mit DTOs arbeiten, niemals Entities weitergeben oder annehmen.

/**
 * Domain service for the UserAccount aggregate.
 *
 * Responsibilities:
 * - user creation
 * - credential verification
 *
 * This service:
 * - orchestrates entity persistence
 * - performs password hashing and verification
 * - contains no JWT logic
 * - contains no HTTP logic
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountDomainService {

    private final UserAccountEntityService userAccountEntityService;
    private final PasswordEncoder passwordEncoder;

    public UserAccount createUser(String username, String rawPassword) {

        Optional<UserAccount> existing =
                userAccountEntityService.loadByUsername(username);

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists: " + username
            );
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userAccountEntityService.save(user);
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String username, String rawPassword) {

        UserAccount user = userAccountEntityService
                .loadByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        boolean matches =
                passwordEncoder.matches(rawPassword, user.getPasswordHash());

        if (!matches) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}