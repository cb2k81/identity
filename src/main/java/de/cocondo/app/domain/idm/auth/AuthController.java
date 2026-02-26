package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.user.InvalidCredentialsException;
import de.cocondo.app.domain.idm.user.dto.AuthenticateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.AuthenticatedUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountAuthenticationService authenticationService;
    private final IdmTokenService idmTokenService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {

        AuthenticateUserRequestDTO authRequest = new AuthenticateUserRequestDTO();
        authRequest.setUsername(request.getUsername());
        authRequest.setPassword(request.getPassword());
        authRequest.setApplicationKey(request.getApplicationKey());
        authRequest.setStageKey(request.getStageKey());

        AuthenticatedUserDTO user;

        try {
            user = authenticationService.authenticate(authRequest);
        } catch (InvalidCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials", ex);
        }

        IdmTokenService.IssuedToken issued = idmTokenService.issueToken(user);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(issued.token());
        response.setExpiresAt(issued.expiresAt());

        return response;
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