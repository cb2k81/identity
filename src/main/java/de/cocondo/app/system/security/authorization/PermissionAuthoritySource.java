package de.cocondo.app.system.security.authorization;

import java.util.List;

/**
 * Technical abstraction for resolving effective permission names for a request context.
 *
 * Responsibilities:
 * - Provide already expanded permission names for the given application/stage/roles context
 * - Stay independent from application-specific domain packages
 *
 * Notes:
 * - Returned values must be raw permission names (e.g. IDM_SCOPE_READ)
 * - Filtering, normalization, deduplication and ordering are handled by the caller
 */
public interface PermissionAuthoritySource {

    List<String> loadPermissionNames(String applicationKey, String stageKey, List<String> roleNames);

}