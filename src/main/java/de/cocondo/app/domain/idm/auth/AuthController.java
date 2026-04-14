package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.domain.idm.auth.dto.LoginResponseDTO;
import de.cocondo.app.domain.idm.auth.dto.MeResponseDTO;
import de.cocondo.app.domain.idm.auth.dto.RefreshRequestDTO;
import de.cocondo.app.domain.idm.auth.session.AuthSession;
import de.cocondo.app.domain.idm.auth.session.AuthSessionLifecycleService;
import de.cocondo.app.domain.idm.auth.session.IssuedAuthSession;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.InvalidCredentialsException;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.dto.AuthenticateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String LOGOUT_REASON = "logout";

    private final UserAccountAuthenticationService authenticationService;
    private final IdmTokenService idmTokenService;
    private final AuthSessionLifecycleService authSessionLifecycleService;
    private final UserAccountEntityService userAccountEntityService;
    private final ApplicationScopeEntityService applicationScopeEntityService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {

        AuthenticateUserRequestDTO authRequest = new AuthenticateUserRequestDTO();
        authRequest.setUsername(request.getUsername());
        authRequest.setPassword(request.getPassword());
        authRequest.setApplicationKey(request.getApplicationKey());
        authRequest.setStageKey(request.getStageKey());

        AuthenticatedUserDTO authenticatedUser;

        try {
            authenticatedUser = authenticationService.authenticate(authRequest);
        } catch (InvalidCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }

        UserAccount userAccount = userAccountEntityService.loadById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated UserAccount could not be reloaded: id=" + authenticatedUser.getId()
                ));

        ApplicationScope applicationScope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(
                        authenticatedUser.getApplicationKey(),
                        authenticatedUser.getStageKey()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated ApplicationScope could not be reloaded: applicationKey="
                                + authenticatedUser.getApplicationKey()
                                + ", stageKey=" + authenticatedUser.getStageKey()
                ));

        IssuedAuthSession issuedAuthSession =
                authSessionLifecycleService.createSession(userAccount, applicationScope);

        IdmTokenService.IssuedToken issuedToken = idmTokenService.issueToken(authenticatedUser);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(issuedToken.token());
        response.setExpiresAt(issuedToken.expiresAt());
        response.setRefreshToken(issuedAuthSession.refreshToken());
        response.setRefreshExpiresAt(issuedAuthSession.refreshExpiresAt());

        return response;
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@RequestBody RefreshRequestDTO request) {

        try {
            AuthSession authSession = authSessionLifecycleService.validateActiveSession(request.getRefreshToken());

            UserAccount userAccount = userAccountEntityService.loadById(authSession.getUserAccount().getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "AuthSession UserAccount could not be reloaded: sessionId=" + authSession.getId()
                    ));

            ApplicationScope applicationScope = applicationScopeEntityService.loadById(authSession.getApplicationScope().getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "AuthSession ApplicationScope could not be reloaded: sessionId=" + authSession.getId()
                    ));

            AuthenticatedUserDTO authenticatedUser =
                    authenticationService.buildAuthenticatedUser(userAccount, applicationScope);

            IdmTokenService.IssuedToken issuedToken = idmTokenService.issueToken(authenticatedUser);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(issuedToken.token());
            response.setExpiresAt(issuedToken.expiresAt());
            response.setRefreshToken(request.getRefreshToken());
            response.setRefreshExpiresAt(authSession.getExpiresAt().toEpochMilli());

            return response;

        } catch (InvalidCredentialsException | IllegalArgumentException | IllegalStateException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshRequestDTO request) {

        try {
            authSessionLifecycleService.revokeSession(request.getRefreshToken(), LOGOUT_REASON);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }
    }

    @GetMapping("/me")
    public MeResponseDTO me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        MeResponseDTO response = new MeResponseDTO();
        response.setUsername(authentication.getName());

        return response;
    }
}