package de.cocondo.app.system.security.jwt;

import de.cocondo.app.system.security.authorization.PermissionResolver;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter (technical).
 *
 * Responsibilities:
 * - Read Authorization header (Bearer token)
 * - Validate token signature
 * - Parse claims
 * - Populate Spring SecurityContext with Authentication
 *
 * Authorities/permissions are resolved via PermissionResolver based on token claims.
 * TODO-ARCH: Token expiration and revocation checks (domain-driven) are out of scope for MVP.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PermissionResolver permissionResolver;

    public JwtAuthenticationFilter(JwtService jwtService, PermissionResolver permissionResolver) {
        this.jwtService = jwtService;
        this.permissionResolver = permissionResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseToken(token);

            // MVP claims (as issued by IdmTokenService)
            String userId = claims.get("sub", String.class);
            String username = claims.get("username", String.class);

            // TODO-ARCH: Decide canonical principal (id vs username). For MVP we prefer username if present.
            String principal = (username != null && !username.isBlank()) ? username : userId;

            if (principal != null && !principal.isBlank()
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<String> roleNames = readStringListClaim(claims, "roles");
                String applicationKey = claims.get("applicationKey", String.class);
                String stageKey = claims.get("stageKey", String.class);

                List<SimpleGrantedAuthority> authorities =
                        permissionResolver.resolveAuthorities(applicationKey, stageKey, roleNames)
                                .stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        "N/A",
                        authorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            // Invalid token → 401
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> readStringListClaim(Claims claims, String name) {

        Object raw = claims.get(name);

        if (raw == null) {
            return Collections.emptyList();
        }

        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(v -> v != null)
                    .map(Object::toString)
                    .filter(v -> !v.isBlank())
                    .toList();
        }

        // tolerate single string claim
        String asString = raw.toString();
        if (asString.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(asString);
    }

}