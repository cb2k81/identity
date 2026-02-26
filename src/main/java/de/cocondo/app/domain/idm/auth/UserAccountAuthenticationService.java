package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.InvalidCredentialsException;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.dto.AuthenticateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AuthenticatedUserDTO authenticate(AuthenticateUserRequestDTO request) {

        validateRequest(request);

        UserAccount user = userAccountEntityService
                .loadByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
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

        List<String> roles = userRoleAssignmentEntityService
                .loadAllByUserAccountId(user.getId())
                .stream()
                .map(a -> a.getRole())
                .filter(r -> r.getApplicationScope().getId().equals(scope.getId()))
                .map(Role::getName)
                .distinct()
                .sorted()
                .toList();

        AuthenticatedUserDTO dto = new AuthenticatedUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setRoles(roles);

        log.info("User authenticated: userId={}, username={}, scope={}/{}",
                user.getId(),
                user.getUsername(),
                scope.getApplicationKey(),
                scope.getStageKey());

        return dto;
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