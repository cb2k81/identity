package de.cocondo.app.system.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter (technical).
 *
 * Responsibilities:
 * - Read Authorization header (Bearer token)
 * - Validate token signature
 * - Parse claims
 * - Populate Spring SecurityContext with Authentication
 *
 * TODO-ARCH: Authorities/permissions mapping will be added later when IDM introduces a permission model.
 * TODO-ARCH: Token expiration and revocation checks (domain-driven) are out of scope for MVP.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        "N/A",
                        Collections.emptyList()
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
}