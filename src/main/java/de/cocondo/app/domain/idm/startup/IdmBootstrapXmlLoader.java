package de.cocondo.app.domain.idm.startup;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads bootstrap XML configuration from classpath resources.
 *
 * Step 1: parsing only (no persistence).
 */
@Component
@RequiredArgsConstructor
public class IdmBootstrapXmlLoader {

    private static final Logger logger = LoggerFactory.getLogger(IdmBootstrapXmlLoader.class);

    private final IdmBootstrapProperties properties;

    private final XmlMapper xmlMapper = new XmlMapper();

    public AdminUserXml loadAdminUserOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getAdminXml(),
                AdminUserXml.class,
                "admin"
        );
    }

    public UsersXml loadUsersOrNull() {
        return loadUsersOrNull(properties.getBasePath());
    }

    public UsersXml loadUsersOrNull(String basePath) {
        return readXmlOrNull(
                basePath,
                properties.getUsersXml(),
                UsersXml.class,
                "users"
        );
    }

    public ApplicationScopesXml loadApplicationScopesOrNull() {
        return loadApplicationScopesOrNull(properties.getBasePath());
    }

    public ApplicationScopesXml loadApplicationScopesOrNull(String basePath) {
        return readXmlOrNull(
                basePath,
                properties.getScopesXml(),
                ApplicationScopesXml.class,
                "scopes"
        );
    }

    public UserApplicationScopeAssignmentsXml loadUserApplicationScopeAssignmentsOrNull() {
        return loadUserApplicationScopeAssignmentsOrNull(properties.getBasePath());
    }

    public UserApplicationScopeAssignmentsXml loadUserApplicationScopeAssignmentsOrNull(String basePath) {
        return readXmlOrNull(
                basePath,
                properties.getUserApplicationScopeAssignmentsXml(),
                UserApplicationScopeAssignmentsXml.class,
                "user-application-scope assignments"
        );
    }

    public PermissionGroupsXml loadPermissionGroupsOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getPermissionGroupsXml(),
                PermissionGroupsXml.class,
                "permission groups"
        );
    }

    public PermissionsXml loadPermissionsOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getPermissionsXml(),
                PermissionsXml.class,
                "permissions"
        );
    }

    public RolesXml loadRolesOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getRolesXml(),
                RolesXml.class,
                "roles"
        );
    }

    public ScopedRolesXml loadScopedRolesOrNull() {
        return loadScopedRolesOrNull(properties.getBasePath());
    }

    public ScopedRolesXml loadScopedRolesOrNull(String basePath) {
        return readXmlOrNull(
                basePath,
                properties.getScopedRolesXml(),
                ScopedRolesXml.class,
                "scoped roles"
        );
    }

    public RolePermissionAssignmentsXml loadRolePermissionAssignmentsOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getRolePermissionAssignmentsXml(),
                RolePermissionAssignmentsXml.class,
                "role-permission assignments"
        );
    }

    public UserRoleAssignmentsXml loadUserRoleAssignmentsOrNull() {
        return readXmlOrNull(
                properties.getBasePath(),
                properties.getUserRoleAssignmentsXml(),
                UserRoleAssignmentsXml.class,
                "user-role assignments"
        );
    }

    public ScopedUserRoleAssignmentsXml loadScopedUserRoleAssignmentsOrNull() {
        return loadScopedUserRoleAssignmentsOrNull(properties.getBasePath());
    }

    public ScopedUserRoleAssignmentsXml loadScopedUserRoleAssignmentsOrNull(String basePath) {
        return readXmlOrNull(
                basePath,
                properties.getScopedUserRoleAssignmentsXml(),
                ScopedUserRoleAssignmentsXml.class,
                "scoped user-role assignments"
        );
    }

    private <T> T readXmlOrNull(String basePath, String fileName, Class<T> type, String label) {
        String path = buildPath(basePath, fileName);
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap {} XML not found at classpath:{} (skipping)", label, path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            T xml = xmlMapper.readValue(in, type);
            logger.info("IDM bootstrap {} XML loaded from classpath:{}", label, path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap " + label + " XML at classpath:" + path, e);
        }
    }

    private static String buildPath(String basePath, String fileName) {
        String base = basePath == null ? "" : basePath.trim();
        String file = fileName == null ? "" : fileName.trim();

        if (base.startsWith("/")) {
            base = base.substring(1);
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (file.startsWith("/")) {
            file = file.substring(1);
        }

        if (base.isEmpty()) {
            return file;
        }
        if (file.isEmpty()) {
            return base;
        }
        return base + "/" + file;
    }
}