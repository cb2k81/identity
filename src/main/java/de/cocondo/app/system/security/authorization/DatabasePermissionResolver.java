package de.cocondo.app.system.security.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Default PermissionResolver implementation backed by a technical authority source.
 *
 * Strategy:
 * - Validate request context
 * - Delegate to PermissionAuthoritySource for effective permission names
 * - Normalize permission names into final authorities used by @PreAuthorize
 *
 * Notes:
 * - The resolver stays in the generic system security kernel
 * - Application-specific domain access must be hidden behind PermissionAuthoritySource
 */
@Service
@RequiredArgsConstructor
public class DatabasePermissionResolver implements PermissionResolver {

    private final PermissionAuthoritySource permissionAuthoritySource;

    @Override
    @Transactional(readOnly = true)
    public List<String> resolveAuthorities(String applicationKey, String stageKey, List<String> roleNames) {

        if (applicationKey == null || applicationKey.isBlank()) {
            return Collections.emptyList();
        }
        if (stageKey == null || stageKey.isBlank()) {
            return Collections.emptyList();
        }
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> permissionNames = permissionAuthoritySource
                .loadPermissionNames(applicationKey, stageKey, roleNames);

        if (permissionNames == null || permissionNames.isEmpty()) {
            return Collections.emptyList();
        }

        return permissionNames.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();
    }
}