package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignment;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.config.IdmSecurityProperties;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.InvalidCredentialsException;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.UserAccountState;
import de.cocondo.app.domain.idm.user.dto.AuthenticateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountAuthenticationService {

    private final UserAccountEntityService userAccountEntityService;
    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;
    private final PasswordEncoder passwordEncoder;
    private final IdmSecurityProperties idmSecurityProperties;

    @Transactional
    public AuthenticatedUserDTO authenticate(AuthenticateUserRequestDTO request) {

        validateRequest(request);

        UserAccount user = userAccountEntityService
                .loadByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = Instant.now();

        // permanent lock
        if (user.getState() == UserAccountState.LOCKED_PERMANENT) {
            throw new InvalidCredentialsException();
        }

        // temporary lock still active
        if (user.getState() == UserAccountState.LOCKED_TEMPORARY
                && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(now)) {
            throw new InvalidCredentialsException();
        }

        // temporary lock expired → reactivate
        if (user.getState() == UserAccountState.LOCKED_TEMPORARY
                && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(now)) {

            user.activate();
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userAccountEntityService.save(user);
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            handleFailedLogin(user);

            throw new InvalidCredentialsException();
        }

        // successful login → reset counters
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            if (user.getState() == UserAccountState.LOCKED_TEMPORARY) {
                user.activate();
            }

            userAccountEntityService.save(user);
        }

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(
                        request.getApplicationKey(),
                        request.getStageKey()
                )
                .orElseThrow(InvalidCredentialsException::new);

        boolean hasAccess = userApplicationScopeAssignmentEntityService
                .existsByUserAccountIdAndApplicationScopeId(
                        user.getId(),
                        scope.getId()
                );

        if (!hasAccess) {
            throw new InvalidCredentialsException();
        }

        List<UserRoleAssignment> assignments = userRoleAssignmentEntityService.loadAllByUserAccountId(user.getId());

        log.info(
                "Authentication role resolution start: userId={}, username={}, requestedScope=({},{}) scopeId={}, assignmentCount={}",
                user.getId(),
                user.getUsername(),
                scope.getApplicationKey(),
                scope.getStageKey(),
                scope.getId(),
                assignments.size()
        );

        assignments.forEach(assignment -> {
            Role role = assignment.getRole();
            ApplicationScope roleScope = role != null ? role.getApplicationScope() : null;

            log.info(
                    "Authentication assignment candidate: userId={}, username={}, assignmentId={}, roleId={}, roleName={}, roleScopeId={}, roleScope=({},{})",
                    user.getId(),
                    user.getUsername(),
                    assignment.getId(),
                    role != null ? role.getId() : null,
                    role != null ? role.getName() : null,
                    roleScope != null ? roleScope.getId() : null,
                    roleScope != null ? roleScope.getApplicationKey() : null,
                    roleScope != null ? roleScope.getStageKey() : null
            );
        });

        List<String> roles = assignments.stream()
                .map(UserRoleAssignment::getRole)
                .filter(role -> role != null && role.getApplicationScope() != null)
                .filter(role -> scope.getApplicationKey().equals(role.getApplicationScope().getApplicationKey()))
                .filter(role -> scope.getStageKey().equals(role.getApplicationScope().getStageKey()))
                .map(Role::getName)
                .distinct()
                .sorted()
                .toList();

        log.info(
                "Authentication role resolution result: userId={}, username={}, requestedScope=({},{}) scopeId={}, roles={}",
                user.getId(),
                user.getUsername(),
                scope.getApplicationKey(),
                scope.getStageKey(),
                scope.getId(),
                roles
        );

        AuthenticatedUserDTO dto = new AuthenticatedUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setRoles(roles);

        log.info(
                "User authenticated: userId={}, username={}, scope={}/{}, roles={}",
                dto.getId(),
                dto.getUsername(),
                dto.getApplicationKey(),
                dto.getStageKey(),
                dto.getRoles()
        );

        return dto;
    }

    private void handleFailedLogin(UserAccount user) {

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        int maxAttempts = idmSecurityProperties
                .getLoginProtection()
                .getMaxFailedAttempts();

        if (attempts >= maxAttempts) {

            long lockSeconds = idmSecurityProperties
                    .getLoginProtection()
                    .getLockDurationSeconds();

            Instant lockedUntil = Instant.now().plusSeconds(lockSeconds);

            user.lockTemporarily();
            user.setLockedUntil(lockedUntil);

            log.warn("User temporarily locked due to failed logins: userId={}, username={}",
                    user.getId(),
                    user.getUsername());
        }

        userAccountEntityService.save(user);
    }

    private void validateRequest(AuthenticateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (isBlank(request.getUsername())) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("password must not be blank");
        }
        if (isBlank(request.getApplicationKey())) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (isBlank(request.getStageKey())) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}