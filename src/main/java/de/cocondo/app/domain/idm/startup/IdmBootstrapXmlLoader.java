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
        String path = buildPath(properties.getBasePath(), properties.getAdminXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap admin XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            AdminUserXml admin = xmlMapper.readValue(in, AdminUserXml.class);
            logger.info("IDM bootstrap admin XML loaded from classpath:{}", path);
            return admin;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap admin XML at classpath:" + path, e);
        }
    }

    public ApplicationScopesXml loadApplicationScopesOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getScopesXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap scopes XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            ApplicationScopesXml scopes = xmlMapper.readValue(in, ApplicationScopesXml.class);
            logger.info("IDM bootstrap scopes XML loaded from classpath:{}", path);
            return scopes;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap scopes XML at classpath:" + path, e);
        }
    }

    public PermissionGroupsXml loadPermissionGroupsOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getPermissionGroupsXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap permission groups XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            PermissionGroupsXml xml = xmlMapper.readValue(in, PermissionGroupsXml.class);
            logger.info("IDM bootstrap permission groups XML loaded from classpath:{}", path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap permission groups XML at classpath:" + path, e);
        }
    }

    public PermissionsXml loadPermissionsOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getPermissionsXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap permissions XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            PermissionsXml xml = xmlMapper.readValue(in, PermissionsXml.class);
            logger.info("IDM bootstrap permissions XML loaded from classpath:{}", path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap permissions XML at classpath:" + path, e);
        }
    }

    public RolesXml loadRolesOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getRolesXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap roles XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            RolesXml xml = xmlMapper.readValue(in, RolesXml.class);
            logger.info("IDM bootstrap roles XML loaded from classpath:{}", path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap roles XML at classpath:" + path, e);
        }
    }

    public RolePermissionAssignmentsXml loadRolePermissionAssignmentsOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getRolePermissionAssignmentsXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap role-permission assignments XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            RolePermissionAssignmentsXml xml = xmlMapper.readValue(in, RolePermissionAssignmentsXml.class);
            logger.info("IDM bootstrap role-permission assignments XML loaded from classpath:{}", path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap role-permission assignments XML at classpath:" + path, e);
        }
    }

    public UserRoleAssignmentsXml loadUserRoleAssignmentsOrNull() {
        String path = buildPath(properties.getBasePath(), properties.getUserRoleAssignmentsXml());
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            logger.info("IDM bootstrap user-role assignments XML not found at classpath:{} (skipping)", path);
            return null;
        }

        try (InputStream in = resource.getInputStream()) {
            UserRoleAssignmentsXml xml = xmlMapper.readValue(in, UserRoleAssignmentsXml.class);
            logger.info("IDM bootstrap user-role assignments XML loaded from classpath:{}", path);
            return xml;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read IDM bootstrap user-role assignments XML at classpath:" + path, e);
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