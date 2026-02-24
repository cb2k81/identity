package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.user.dto.AuthenticateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Domain service for the UserAccount aggregate.
 *
 * Responsibilities:
 * - user creation
 * - credential verification (scope-aware)
 *
 * This service:
 * - orchestrates entity persistence
 * - performs password hashing and verification
 * - contains no JWT logic
 * - contains no HTTP logic
 *
 * Architectural constraint:
 * - Domain services expose only DTOs across boundaries (no entities).
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserAccountDomainService {

    private final UserAccountEntityService userAccountEntityService;
    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountDTO createUser(CreateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        Optional<UserAccount> existing =
                userAccountEntityService.loadByUsername(request.getUsername());

        if (existing.isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.activate();

        UserAccount saved = userAccountEntityService.save(user);

        UserAccountDTO dto = new UserAccountDTO();
        dto.setId(saved.getId());
        dto.setUsername(saved.getUsername());
        dto.setState(saved.getState());

        log.info("User created: id={}, username={}, state={}", saved.getId(), saved.getUsername(), saved.getState());

        return dto;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserDTO authenticate(AuthenticateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        if (request.getApplicationKey() == null || request.getApplicationKey().isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (request.getStageKey() == null || request.getStageKey().isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        UserAccount user = userAccountEntityService
                .loadByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            // Do not leak account status details in auth response.
            throw new InvalidCredentialsException();
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new InvalidCredentialsException();
        }

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(request.getApplicationKey(), request.getStageKey())
                .orElseThrow(InvalidCredentialsException::new);

        boolean hasAccess =
                userApplicationScopeAssignmentEntityService
                        .existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId());

        if (!hasAccess) {
            // Do not leak existence of scope/user mapping.
            throw new InvalidCredentialsException();
        }

        AuthenticatedUserDTO dto = new AuthenticatedUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());

        List<String> roles = userRoleAssignmentEntityService
                .loadAllByUserAccountId(user.getId())
                .stream()
                .map(a -> a.getRole())
                .filter(r -> r.getApplicationScope().getId().equals(scope.getId()))
                .map(Role::getName)
                .distinct()
                .sorted()
                .toList();

        dto.setRoles(roles);

        log.info("User authenticated: userId={}, username={}, applicationKey={}, stageKey={}",
                user.getId(), user.getUsername(), scope.getApplicationKey(), scope.getStageKey());

        return dto;
    }
}