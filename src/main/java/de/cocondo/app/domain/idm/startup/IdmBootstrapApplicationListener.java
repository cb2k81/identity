package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignment;
import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignment;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignment;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroup;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdmBootstrapApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final IdmBootstrapProperties bootstrapProperties;
    private final IdmSelfProperties selfProperties;
    private final IdmBootstrapXmlLoader xmlLoader;
    private final IdmAdditionalBundleBootstrapService additionalBundleBootstrapService;

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserAccountEntityService userAccountEntityService;
    private final UserApplicationScopeAssignmentEntityService assignmentEntityService;

    // Authorization bootstrap (Self-Scope only + foreign-scope roles)
    private final PermissionGroupEntityService permissionGroupEntityService;
    private final PermissionEntityService permissionEntityService;
    private final RoleEntityService roleEntityService;
    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {

        if (!bootstrapProperties.isEnabled()) {
            log.info("IDM bootstrap disabled (idm.bootstrap.enabled=false).");
            return;
        }

        String mode = normalizeMode(bootstrapProperties.getMode());
        boolean force = "force".equals(mode);

        String selfAppKey = required(selfProperties.getScope().getApplicationKey(), "idm.self.scope.application-key");
        String selfStageKey = required(selfProperties.getScope().getStageKey(), "idm.self.scope.stage-key");

        log.info("IDM bootstrap starting (mode={}, selfScope=({},{}))", mode, selfAppKey, selfStageKey);

        // 1) Load scopes from XML (source of truth)
        ApplicationScopesXml scopesXml = xmlLoader.loadApplicationScopesOrNull();
        if (scopesXml == null || scopesXml.getItems() == null || scopesXml.getItems().isEmpty()) {
            throw new IllegalStateException("IDM bootstrap enabled but scopes XML is missing/empty. " +
                    "Expected at classpath:" + bootstrapProperties.getBasePath() + "/" + bootstrapProperties.getScopesXml());
        }

        List<ApplicationScope> ensuredScopes = scopesXml.getItems().stream()
                .map(it -> ensureScope(it, force))
                .toList();

        ApplicationScopesXml.ApplicationScopeXmlItem selfScopeXml = scopesXml.getItems().stream()
                .filter(it -> selfAppKey.equals(it.getApplicationKey()) && selfStageKey.equals(it.getStageKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Self scope not found in scopes XML: (" + selfAppKey + "," + selfStageKey + ")"));

        ApplicationScope selfScope = ensuredScopes.stream()
                .filter(it -> selfAppKey.equals(it.getApplicationKey()) && selfStageKey.equals(it.getStageKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Self scope not found after ensureScope(): (" + selfAppKey + "," + selfStageKey + ")"));

        // selfScopeXml is intentionally resolved and validated as part of bootstrap source-of-truth checks
        if (selfScopeXml == null) {
            throw new IllegalStateException("Self scope XML resolution unexpectedly returned null.");
        }

        // 2) Resolve admin config (XML preferred, properties fallback)
        AdminUserXml adminXml = xmlLoader.loadAdminUserOrNull();
        String adminUsername;
        String adminDisplayName = null;
        String adminEmail = null;
        String adminPassword;

        if (adminXml != null) {
            adminUsername = required(adminXml.getUsername(), "bootstrap admin XML: username");
            adminDisplayName = adminXml.getDisplayName();
            adminEmail = adminXml.getEmail();
            adminPassword = required(adminXml.getPassword(), "bootstrap admin XML: password");
        } else {
            adminUsername = required(bootstrapProperties.getAdmin().getUsername(), "idm.bootstrap.admin.username");
            adminPassword = required(bootstrapProperties.getAdmin().getPassword(), "idm.bootstrap.admin.password");
        }

        // 3) Ensure admin user exists (+ optional force reset)
        UserAccount adminUser = ensureUser(adminUsername, adminDisplayName, adminEmail, adminPassword, force, "admin user");

        // 4) Ensure assignment admin -> self scope exists
        ensureUserApplicationScopeAssignment(adminUser, selfScope, "admin assignment");

        // 5) Ensure additional users from XML (optional)
        UsersXml usersXml = xmlLoader.loadUsersOrNull();
        if (usersXml == null || usersXml.getItems() == null || usersXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: users XML missing/empty (skipping additional users).");
        } else {
            usersXml.getItems().forEach(it -> ensureAdditionalUser(it, force));
        }

        // 6) Ensure additional user -> scope assignments from XML (optional)
        UserApplicationScopeAssignmentsXml userScopeXml = xmlLoader.loadUserApplicationScopeAssignmentsOrNull();
        if (userScopeXml == null || userScopeXml.getItems() == null || userScopeXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: user-application-scope assignments XML missing/empty (skipping user-application-scope assignments).");
        } else {
            userScopeXml.getItems().forEach(this::ensureUserApplicationScopeAssignment);
        }

        // 7) Authorization bootstrap for Self-Scope only
        ensureAuthorizationBootstrap(selfScope, force);

        // 8) Foreign-scope roles bootstrap (optional)
        ScopedRolesXml scopedRolesXml = xmlLoader.loadScopedRolesOrNull();
        if (scopedRolesXml == null || scopedRolesXml.getItems() == null || scopedRolesXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: scoped roles XML missing/empty (skipping scoped roles).");
        } else {
            scopedRolesXml.getItems().forEach(it -> ensureScopedRole(it, force));
        }

        // 9) Foreign-scope user-role assignments bootstrap (optional)
        ScopedUserRoleAssignmentsXml scopedUserRoleAssignmentsXml = xmlLoader.loadScopedUserRoleAssignmentsOrNull();
        if (scopedUserRoleAssignmentsXml == null || scopedUserRoleAssignmentsXml.getItems() == null || scopedUserRoleAssignmentsXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: scoped user-role assignments XML missing/empty (skipping scoped user-role assignments).");
        } else {
            scopedUserRoleAssignmentsXml.getItems().forEach(this::ensureScopedUserRoleAssignment);
        }

        // 10) Additional generic bootstrap bundles (optional)
        additionalBundleBootstrapService.processAdditionalBundles(force);

        log.info("IDM bootstrap completed (mode={}, selfScope=({},{}), admin={})",
                mode, selfAppKey, selfStageKey, adminUsername);
    }

    private void ensureAuthorizationBootstrap(ApplicationScope selfScope, boolean force) {

        // PermissionGroups (optional XML, but recommended)
        PermissionGroupsXml permissionGroupsXml = xmlLoader.loadPermissionGroupsOrNull();
        if (permissionGroupsXml == null || permissionGroupsXml.getItems() == null || permissionGroupsXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: permission groups XML missing/empty (skipping permission groups).");
        } else {
            permissionGroupsXml.getItems().forEach(it -> ensurePermissionGroup(selfScope, it, force));
        }

        // Permissions (optional XML, but recommended)
        PermissionsXml permissionsXml = xmlLoader.loadPermissionsOrNull();
        if (permissionsXml == null || permissionsXml.getItems() == null || permissionsXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: permissions XML missing/empty (skipping permissions).");
        } else {
            permissionsXml.getItems().forEach(it -> ensurePermission(selfScope, it, force));
        }

        // Roles (optional XML, but recommended)
        RolesXml rolesXml = xmlLoader.loadRolesOrNull();
        if (rolesXml == null || rolesXml.getItems() == null || rolesXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: roles XML missing/empty (skipping roles).");
        } else {
            rolesXml.getItems().forEach(it -> ensureRole(selfScope, it, force));
        }

        // RolePermissionAssignments (optional XML)
        RolePermissionAssignmentsXml rolePermXml = xmlLoader.loadRolePermissionAssignmentsOrNull();
        if (rolePermXml == null || rolePermXml.getItems() == null || rolePermXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: role-permission assignments XML missing/empty (skipping role-permission assignments).");
        } else {
            rolePermXml.getItems().forEach(it -> ensureRolePermissionAssignment(selfScope, it));
        }

        // UserRoleAssignments (optional XML)
        UserRoleAssignmentsXml userRoleXml = xmlLoader.loadUserRoleAssignmentsOrNull();
        if (userRoleXml == null || userRoleXml.getItems() == null || userRoleXml.getItems().isEmpty()) {
            log.info("IDM bootstrap: user-role assignments XML missing/empty (skipping user-role assignments).");
        } else {
            userRoleXml.getItems().forEach(it -> ensureUserRoleAssignment(selfScope, it));
        }
    }

    private void ensureAdditionalUser(UsersXml.UserXmlItem xmlItem, boolean force) {
        String username = required(xmlItem.getUsername(), "users XML: username");
        String displayName = xmlItem.getDisplayName();
        String email = xmlItem.getEmail();
        String password = required(xmlItem.getPassword(), "users XML: password");

        ensureUser(username, displayName, email, password, force, "additional user");
    }

    private PermissionGroup ensurePermissionGroup(
            ApplicationScope scope,
            PermissionGroupsXml.PermissionGroupXmlItem xmlItem,
            boolean force
    ) {

        String name = required(xmlItem.getName(), "permissionGroups XML: name");
        String description = xmlItem.getDescription();

        Optional<PermissionGroup> existingOpt =
                permissionGroupEntityService.loadByApplicationScopeIdAndName(scope.getId(), name);

        if (existingOpt.isEmpty()) {
            PermissionGroup created = new PermissionGroup();
            created.setApplicationScope(scope);
            created.setName(name);
            created.setDescription(description);

            PermissionGroup saved = permissionGroupEntityService.save(created);
            log.info("Created PermissionGroup: id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
            return saved;
        }

        PermissionGroup existing = existingOpt.get();

        if (force) {
            if (!Objects.equals(existing.getDescription(), description)) {
                existing.setDescription(description);
                PermissionGroup saved = permissionGroupEntityService.save(existing);
                log.info("Updated PermissionGroup (force): id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
                return saved;
            }
        }

        log.info("PermissionGroup exists: id={}, scopeId={}, name={}", existing.getId(), scope.getId(), existing.getName());
        return existing;
    }

    private Permission ensurePermission(
            ApplicationScope scope,
            PermissionsXml.PermissionXmlItem xmlItem,
            boolean force
    ) {

        String groupName = required(xmlItem.getPermissionGroupName(), "permissions XML: permissionGroupName");
        String name = required(xmlItem.getName(), "permissions XML: name");
        String description = xmlItem.getDescription();

        // systemProtected MUST be true for IDM core bootstrap data
        boolean systemProtected = true;

        PermissionGroup group = permissionGroupEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), groupName)
                .orElseThrow(() -> new IllegalStateException(
                        "PermissionGroup not found for permission bootstrap: groupName=" + groupName));

        Optional<Permission> existingOpt =
                permissionEntityService.loadByApplicationScopeIdAndName(scope.getId(), name);

        if (existingOpt.isEmpty()) {
            Permission created = new Permission();
            created.setApplicationScope(scope);
            created.setPermissionGroup(group);
            created.setName(name);
            created.setDescription(description);
            created.setSystemProtected(systemProtected);

            Permission saved = permissionEntityService.save(created);
            log.info("Created Permission: id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
            return saved;
        }

        Permission existing = existingOpt.get();

        if (force) {
            boolean changed = false;

            if (!Objects.equals(existing.getDescription(), description)) {
                existing.setDescription(description);
                changed = true;
            }

            // enforce systemProtected=true (hard requirement)
            if (!existing.isSystemProtected()) {
                existing.setSystemProtected(true);
                changed = true;
            }

            if (changed) {
                Permission saved = permissionEntityService.save(existing);
                log.info("Updated Permission (force): id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
                return saved;
            }
        }

        log.info("Permission exists: id={}, scopeId={}, name={}", existing.getId(), scope.getId(), existing.getName());
        return existing;
    }

    private Role ensureRole(
            ApplicationScope scope,
            RolesXml.RoleXmlItem xmlItem,
            boolean force
    ) {

        String name = required(xmlItem.getName(), "roles XML: name");
        String description = xmlItem.getDescription();

        // systemProtected MUST be true for IDM core bootstrap roles
        boolean systemProtected = true;

        Optional<Role> existingOpt = roleEntityService.loadByApplicationScopeIdAndName(scope.getId(), name);

        if (existingOpt.isEmpty()) {
            Role created = new Role();
            created.setApplicationScope(scope);
            created.setName(name);
            created.setDescription(description);
            created.setSystemProtected(systemProtected);

            Role saved = roleEntityService.save(created);
            log.info("Created Role: id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
            return saved;
        }

        Role existing = existingOpt.get();

        if (force) {
            boolean changed = false;

            if (!Objects.equals(existing.getDescription(), description)) {
                existing.setDescription(description);
                changed = true;
            }

            // enforce systemProtected=true (hard requirement)
            if (!existing.isSystemProtected()) {
                existing.setSystemProtected(true);
                changed = true;
            }

            if (changed) {
                Role saved = roleEntityService.save(existing);
                log.info("Updated Role (force): id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
                return saved;
            }
        }

        log.info("Role exists: id={}, scopeId={}, name={}", existing.getId(), scope.getId(), existing.getName());
        return existing;
    }

    private Role ensureScopedRole(
            ScopedRolesXml.ScopedRoleXmlItem xmlItem,
            boolean force
    ) {

        String applicationKey = required(xmlItem.getApplicationKey(), "scopedRoles XML: applicationKey");
        String stageKey = required(xmlItem.getStageKey(), "scopedRoles XML: stageKey");
        String name = required(xmlItem.getName(), "scopedRoles XML: name");
        String description = xmlItem.getDescription();
        boolean systemProtected = xmlItem.isSystemProtected();

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new IllegalStateException(
                        "ApplicationScope not found for scoped role bootstrap: (" + applicationKey + "," + stageKey + ")"));

        Optional<Role> existingOpt = roleEntityService.loadByApplicationScopeIdAndName(scope.getId(), name);

        if (existingOpt.isEmpty()) {
            Role created = new Role();
            created.setApplicationScope(scope);
            created.setName(name);
            created.setDescription(description);
            created.setSystemProtected(systemProtected);

            Role saved = roleEntityService.save(created);
            log.info("Created scoped Role: id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
            return saved;
        }

        Role existing = existingOpt.get();

        if (force) {
            boolean changed = false;

            if (!Objects.equals(existing.getDescription(), description)) {
                existing.setDescription(description);
                changed = true;
            }

            if (existing.isSystemProtected() != systemProtected) {
                existing.setSystemProtected(systemProtected);
                changed = true;
            }

            if (changed) {
                Role saved = roleEntityService.save(existing);
                log.info("Updated scoped Role (force): id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
                return saved;
            }
        }

        log.info("Scoped Role exists: id={}, scopeId={}, name={}", existing.getId(), scope.getId(), existing.getName());
        return existing;
    }

    private void ensureRolePermissionAssignment(
            ApplicationScope scope,
            RolePermissionAssignmentsXml.RolePermissionAssignmentXmlItem xmlItem
    ) {

        String roleName = required(xmlItem.getRoleName(), "rolePermissionAssignments XML: roleName");
        String permissionName = required(xmlItem.getPermissionName(), "rolePermissionAssignments XML: permissionName");

        Role role = roleEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found for role-permission bootstrap: " + roleName));

        Permission permission = permissionEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), permissionName)
                .orElseThrow(() -> new IllegalStateException("Permission not found for role-permission bootstrap: " + permissionName));

        boolean exists = rolePermissionAssignmentEntityService.loadAllByRoleId(role.getId())
                .stream()
                .anyMatch(a -> a.getPermission().getId().equals(permission.getId()));

        if (exists) {
            log.info("RolePermissionAssignment exists: roleName={}, permissionName={}", roleName, permissionName);
            return;
        }

        RolePermissionAssignment assignment = new RolePermissionAssignment();
        assignment.setRole(role);
        assignment.setPermission(permission);

        RolePermissionAssignment saved = rolePermissionAssignmentEntityService.save(assignment);
        log.info("Created RolePermissionAssignment: id={}, roleName={}, permissionName={}",
                saved.getId(), roleName, permissionName);
    }

    private void ensureUserRoleAssignment(
            ApplicationScope scope,
            UserRoleAssignmentsXml.UserRoleAssignmentXmlItem xmlItem
    ) {

        String username = required(xmlItem.getUsername(), "userRoleAssignments XML: username");
        String roleName = required(xmlItem.getRoleName(), "userRoleAssignments XML: roleName");

        UserAccount user = userAccountEntityService
                .loadByUsername(username)
                .orElseThrow(() -> new IllegalStateException("UserAccount not found for user-role bootstrap: " + username));

        Role role = roleEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found for user-role bootstrap: " + roleName));

        boolean exists = userRoleAssignmentEntityService.loadAllByUserAccountId(user.getId())
                .stream()
                .anyMatch(a -> a.getRole().getId().equals(role.getId()));

        if (exists) {
            log.info("UserRoleAssignment exists: username={}, roleName={}", username, roleName);
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUserAccount(user);
        assignment.setRole(role);

        UserRoleAssignment saved = userRoleAssignmentEntityService.save(assignment);
        log.info("Created UserRoleAssignment: id={}, username={}, roleName={}",
                saved.getId(), username, roleName);
    }

    private void ensureScopedUserRoleAssignment(
            ScopedUserRoleAssignmentsXml.ScopedUserRoleAssignmentXmlItem xmlItem
    ) {

        String username = required(xmlItem.getUsername(), "scopedUserRoleAssignments XML: username");
        String applicationKey = required(xmlItem.getApplicationKey(), "scopedUserRoleAssignments XML: applicationKey");
        String stageKey = required(xmlItem.getStageKey(), "scopedUserRoleAssignments XML: stageKey");
        String roleName = required(xmlItem.getRoleName(), "scopedUserRoleAssignments XML: roleName");

        UserAccount user = userAccountEntityService
                .loadByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "UserAccount not found for scoped user-role bootstrap: " + username));

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new IllegalStateException(
                        "ApplicationScope not found for scoped user-role bootstrap: (" + applicationKey + "," + stageKey + ")"));

        Role role = roleEntityService
                .loadByApplicationScopeIdAndName(scope.getId(), roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Role not found for scoped user-role bootstrap: " + roleName));

        boolean exists = userRoleAssignmentEntityService.loadAllByUserAccountId(user.getId())
                .stream()
                .anyMatch(a -> a.getRole().getId().equals(role.getId()));

        if (exists) {
            log.info("Scoped UserRoleAssignment exists: username={}, applicationKey={}, stageKey={}, roleName={}",
                    username, applicationKey, stageKey, roleName);
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUserAccount(user);
        assignment.setRole(role);

        UserRoleAssignment saved = userRoleAssignmentEntityService.save(assignment);
        log.info("Created scoped UserRoleAssignment: id={}, username={}, applicationKey={}, stageKey={}, roleName={}",
                saved.getId(), username, applicationKey, stageKey, roleName);
    }

    private ApplicationScope ensureScope(ApplicationScopesXml.ApplicationScopeXmlItem xmlItem, boolean force) {

        Optional<ApplicationScope> existingOpt =
                applicationScopeEntityService.loadByApplicationKeyAndStageKey(xmlItem.getApplicationKey(), xmlItem.getStageKey());

        if (existingOpt.isEmpty()) {
            ApplicationScope created = new ApplicationScope();
            created.setApplicationKey(xmlItem.getApplicationKey());
            created.setStageKey(xmlItem.getStageKey());
            created.setDescription(xmlItem.getDescription());

            ApplicationScope saved = applicationScopeEntityService.save(created);
            log.info("Created ApplicationScope: id={}, applicationKey={}, stageKey={}",
                    saved.getId(), saved.getApplicationKey(), saved.getStageKey());
            return saved;
        }

        ApplicationScope existing = existingOpt.get();

        if (force) {
            String newDesc = xmlItem.getDescription();
            if (!Objects.equals(existing.getDescription(), newDesc)) {
                existing.setDescription(newDesc);
                ApplicationScope saved = applicationScopeEntityService.save(existing);
                log.info("Updated ApplicationScope (force): id={}, applicationKey={}, stageKey={}",
                        saved.getId(), saved.getApplicationKey(), saved.getStageKey());
                return saved;
            }
        }

        log.info("ApplicationScope exists: id={}, applicationKey={}, stageKey={}",
                existing.getId(), existing.getApplicationKey(), existing.getStageKey());
        return existing;
    }

    private UserAccount ensureUser(
            String username,
            String displayName,
            String email,
            String rawPassword,
            boolean force,
            String logContext
    ) {

        Optional<UserAccount> existingOpt = userAccountEntityService.loadByUsername(username);

        if (existingOpt.isEmpty()) {
            UserAccount created = new UserAccount();
            created.setUsername(username);
            created.setDisplayName(displayName);
            created.setEmail(email);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            created.activate();

            UserAccount saved = userAccountEntityService.save(created);
            log.info("Created {}: id={}, username={}", logContext, saved.getId(), saved.getUsername());
            return saved;
        }

        UserAccount existing = existingOpt.get();

        if (force) {
            boolean changed = false;

            if (!Objects.equals(existing.getDisplayName(), displayName)) {
                existing.setDisplayName(displayName);
                changed = true;
            }

            if (!Objects.equals(existing.getEmail(), email)) {
                existing.setEmail(email);
                changed = true;
            }

            existing.setPasswordHash(passwordEncoder.encode(rawPassword));
            changed = true;

            if (!existing.isActive()) {
                existing.activate();
                changed = true;
            }

            if (changed) {
                UserAccount saved = userAccountEntityService.save(existing);
                log.info("Updated {} (force): id={}, username={}", logContext, saved.getId(), saved.getUsername());
                return saved;
            }
        }

        log.info("{} exists: id={}, username={}", capitalize(logContext), existing.getId(), existing.getUsername());
        return existing;
    }

    private void ensureUserApplicationScopeAssignment(
            UserAccount user,
            ApplicationScope scope,
            String logContext
    ) {

        boolean exists = assignmentEntityService.existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId());

        if (exists) {
            log.info("{} exists: username={}, applicationKey={}, stageKey={}",
                    capitalize(logContext), user.getUsername(), scope.getApplicationKey(), scope.getStageKey());
            return;
        }

        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(scope);

        UserApplicationScopeAssignment saved = assignmentEntityService.save(assignment);
        log.info("Created {}: id={}, username={}, applicationKey={}, stageKey={}",
                logContext, saved.getId(), user.getUsername(), scope.getApplicationKey(), scope.getStageKey());
    }

    private void ensureUserApplicationScopeAssignment(
            UserApplicationScopeAssignmentsXml.UserApplicationScopeAssignmentXmlItem xmlItem
    ) {

        String username = required(xmlItem.getUsername(), "userApplicationScopeAssignments XML: username");
        String applicationKey = required(xmlItem.getApplicationKey(), "userApplicationScopeAssignments XML: applicationKey");
        String stageKey = required(xmlItem.getStageKey(), "userApplicationScopeAssignments XML: stageKey");

        UserAccount user = userAccountEntityService
                .loadByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "UserAccount not found for user-application-scope bootstrap: " + username));

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new IllegalStateException(
                        "ApplicationScope not found for user-application-scope bootstrap: (" + applicationKey + "," + stageKey + ")"));

        ensureUserApplicationScopeAssignment(user, scope, "user-application-scope assignment");
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return "safe";
        }
        String normalized = mode.trim().toLowerCase();
        return switch (normalized) {
            case "safe", "force" -> normalized;
            default -> throw new IllegalStateException("Unsupported idm.bootstrap.mode: " + mode);
        };
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required value: " + label);
        }
        return value;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}