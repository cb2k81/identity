package de.cocondo.app.system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Development authentication filter.
 *
 * Responsibilities:
 * - Inject a default authenticated user into the SecurityContext
 * - Grant all defined permissions in DEV profile
 *
 * This allows @PreAuthorize checks to work in development
 * without requiring real authentication.
 *
 * Must NOT be active in production.
 */
@Component
@Profile("dev")
public class DevAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            var authorities = List.of(
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_CREATE"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_READ"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_READ_INACTIVE"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_UPDATE"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_DELETE"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_METADATA_READ"),
                    new SimpleGrantedAuthority("PERSONNEL_PERSON_METADATA_UPDATE")
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    "dev-user",
                    "N/A",
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
