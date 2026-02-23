package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Authentication controller for IDM.
 *
 * Responsibilities:
 * - Accept login requests
 * - Delegate authentication to domain service
 * - Issue JWT via IdmTokenService
 * - Expose authenticated identity via /me endpoint
 *
 * Architectural constraints:
 * - No password hashing here
 * - No JWT signing logic here
 * - No business rules here
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountDomainService userAccountDomainService;
    private final IdmTokenService idmTokenService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {

        UserAccount user = userAccountDomainService
                .authenticate(request.getUsername(), request.getPassword());

        String token = idmTokenService.issueToken(user);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        // TODO-ARCH: expiration should be provided by IdmTokenService
        long expiresAt = new Date(System.currentTimeMillis() + 60 * 60 * 1000).getTime();
        response.setExpiresAt(expiresAt);

        return response;
    }

    /**
     * Returns information about the currently authenticated user.
     *
     * This endpoint is protected by JWT authentication.
     *
     * @param authentication injected Spring Security authentication
     * @return MeResponseDTO containing principal information
     */
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