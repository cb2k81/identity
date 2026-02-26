package de.cocondo.app.config;

/**
 * Final authority names for IDM administration use-cases.
 *
 * Naming convention:
 * IDM_<RESOURCE>_<ACTION>
 *
 * Notes:
 * - Stage-specificity is modeled via ApplicationScope (applicationKey + stageKey),
 *   not by encoding stage into the authority string.
 */
public final class IdmManagementAuthorities {

    private IdmManagementAuthorities() {
    }

    public static final String IDM_SCOPE_CREATE = "IDM_SCOPE_CREATE";
    public static final String IDM_SCOPE_READ = "IDM_SCOPE_READ";
    public static final String IDM_SCOPE_UPDATE = "IDM_SCOPE_UPDATE";
    public static final String IDM_SCOPE_DELETE = "IDM_SCOPE_DELETE";

    public static final String IDM_ROLE_CREATE = "IDM_ROLE_CREATE";
    public static final String IDM_ROLE_READ = "IDM_ROLE_READ";
    public static final String IDM_ROLE_UPDATE = "IDM_ROLE_UPDATE";
    public static final String IDM_ROLE_DELETE = "IDM_ROLE_DELETE";

    public static final String IDM_PERMISSION_CREATE = "IDM_PERMISSION_CREATE";
    public static final String IDM_PERMISSION_READ = "IDM_PERMISSION_READ";
    public static final String IDM_PERMISSION_UPDATE = "IDM_PERMISSION_UPDATE";
    public static final String IDM_PERMISSION_DELETE = "IDM_PERMISSION_DELETE";

    public static final String IDM_ROLE_PERMISSION_ASSIGN = "IDM_ROLE_PERMISSION_ASSIGN";
    public static final String IDM_ROLE_PERMISSION_UNASSIGN = "IDM_ROLE_PERMISSION_UNASSIGN";

    public static final String IDM_USER_CREATE = "IDM_USER_CREATE";
    public static final String IDM_USER_READ = "IDM_USER_READ";
    public static final String IDM_USER_UPDATE = "IDM_USER_UPDATE";
    public static final String IDM_USER_DELETE = "IDM_USER_DELETE";

    public static final String IDM_USER_ROLE_ASSIGN = "IDM_USER_ROLE_ASSIGN";
    public static final String IDM_USER_ROLE_UNASSIGN = "IDM_USER_ROLE_UNASSIGN";
}