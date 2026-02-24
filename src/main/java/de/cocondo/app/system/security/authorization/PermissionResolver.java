package de.cocondo.app.system.security.authorization;

import java.util.List;

/**
 * Resolves effective authorities (permissions) for the current request context.
 *
 * Input is derived from token claims:
 * - applicationKey
 * - stageKey
 * - role names
 *
 * Output must match the authority strings used in @PreAuthorize checks (e.g. IdmManagementAuthorities.*).
 */
public interface PermissionResolver {

    List<String> resolveAuthorities(String applicationKey, String stageKey, List<String> roleNames);
}