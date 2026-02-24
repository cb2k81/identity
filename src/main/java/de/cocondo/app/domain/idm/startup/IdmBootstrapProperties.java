package de.cocondo.app.domain.idm.startup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for IDM bootstrap initialization.
 *
 * idm.bootstrap.*
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "idm.bootstrap")
public class IdmBootstrapProperties {

    /**
     * Enables or disables bootstrap execution.
     */
    private boolean enabled = false;

    /**
     * Bootstrap execution mode.
     * Supported:
     * - safe  : create missing data, never overwrite existing data
     * - force : create missing data and update specific fields if different
     */
    private String mode = "safe";

    /**
     * Base classpath folder for bootstrap XML files.
     * Example: "idm/bootstrap"
     */
    private String basePath = "idm/bootstrap";

    /**
     * Admin user XML file name within basePath.
     * Example: "admin.xml"
     */
    private String adminXml = "admin.xml";

    /**
     * Application scopes XML file name within basePath.
     * Example: "scopes.xml"
     */
    private String scopesXml = "scopes.xml";

    /**
     * Permission groups XML file name within basePath.
     * Example: "permission-groups.xml"
     */
    private String permissionGroupsXml = "permission-groups.xml";

    /**
     * Permissions XML file name within basePath.
     * Example: "permissions.xml"
     */
    private String permissionsXml = "permissions.xml";

    /**
     * Roles XML file name within basePath.
     * Example: "roles.xml"
     */
    private String rolesXml = "roles.xml";

    /**
     * Role-Permission assignments XML file name within basePath.
     * Example: "role-permission-assignments.xml"
     */
    private String rolePermissionAssignmentsXml = "role-permission-assignments.xml";

    /**
     * User-Role assignments XML file name within basePath.
     * Example: "user-role-assignments.xml"
     */
    private String userRoleAssignmentsXml = "user-role-assignments.xml";

    /**
     * Default admin configuration (used as fallback if no admin.xml exists).
     */
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {

        private String username = "admin";

        private String password = "admin";
    }
}