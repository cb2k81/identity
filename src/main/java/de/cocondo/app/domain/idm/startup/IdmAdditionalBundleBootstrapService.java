package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignment;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignment;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdmAdditionalBundleBootstrapService {

    private final IdmBootstrapProperties bootstrapProperties;
    private final IdmBootstrapXmlLoader xmlLoader;

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserAccountEntityService userAccountEntityService;
    private final UserApplicationScopeAssignmentEntityService assignmentEntityService;
    private final RoleEntityService roleEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;
    private final PasswordEncoder passwordEncoder;

    public void processAdditionalBundles(boolean force) {
        List<IdmBootstrapProperties.AdditionalBundle> bundles = bootstrapProperties.getAdditionalBundles();
        if (bundles == null || bundles.isEmpty()) {
            log.info("IDM bootstrap: no additional bundles configured.");
            return;
        }

        for (IdmBootstrapProperties.AdditionalBundle bundle : bundles) {
            if (bundle == null) {
                continue;
            }

            if (!bundle.isEnabled()) {
                log.info("IDM bootstrap: additional bundle '{}' disabled (skipping).",
                        optionalTrim(bundle.getName()).orElse("<unnamed>"));
                continue;
            }

            String bundleName = optionalTrim(bundle.getName()).orElse("<unnamed>");
            String bundleBasePath = required(
                    bundle.getBasePath(),
                    "idm.bootstrap.additional-bundles[].base-path"
            );

            log.info("IDM bootstrap: processing additional bundle '{}' from classpath:{}",
                    bundleName, bundleBasePath);

            processAdditionalBundle(bundleBasePath, force);

            log.info("IDM bootstrap: completed additional bundle '{}' from classpath:{}",
                    bundleName, bundleBasePath);
        }
    }

    private void processAdditionalBundle(String bundleBasePath, boolean force) {

        // 1) Optional scopes
        ApplicationScopesXml scopesXml = xmlLoader.loadApplicationScopesOrNull(bundleBasePath);
        if (scopesXml == null || scopesXml.getItems() == null || scopesXml.getItems().isEmpty()) {
            log.info("IDM bootstrap bundle: scopes XML missing/empty (skipping scopes), basePath={}", bundleBasePath);
        } else {
            scopesXml.getItems().forEach(it -> ensureScope(it, force));
        }

        // 2) Optional users
        UsersXml usersXml = xmlLoader.loadUsersOrNull(bundleBasePath);
        if (usersXml == null || usersXml.getItems() == null || usersXml.getItems().isEmpty()) {
            log.info("IDM bootstrap bundle: users XML missing/empty (skipping users), basePath={}", bundleBasePath);
        } else {
            usersXml.getItems().forEach(it -> ensureAdditionalUser(it, force));
        }

        // 3) Optional user -> scope assignments
        UserApplicationScopeAssignmentsXml userScopeXml = xmlLoader.loadUserApplicationScopeAssignmentsOrNull(bundleBasePath);
        if (userScopeXml == null || userScopeXml.getItems() == null || userScopeXml.getItems().isEmpty()) {
            log.info("IDM bootstrap bundle: user-application-scope assignments XML missing/empty (skipping assignments), basePath={}", bundleBasePath);
        } else {
            userScopeXml.getItems().forEach(this::ensureUserApplicationScopeAssignment);
        }

        // 4) Optional foreign-scope roles
        ScopedRolesXml scopedRolesXml = xmlLoader.loadScopedRolesOrNull(bundleBasePath);
        if (scopedRolesXml == null || scopedRolesXml.getItems() == null || scopedRolesXml.getItems().isEmpty()) {
            log.info("IDM bootstrap bundle: scoped roles XML missing/empty (skipping scoped roles), basePath={}", bundleBasePath);
        } else {
            scopedRolesXml.getItems().forEach(it -> ensureScopedRole(it, force));
        }

        // 5) Optional foreign-scope user-role assignments
        ScopedUserRoleAssignmentsXml scopedUserRoleAssignmentsXml = xmlLoader.loadScopedUserRoleAssignmentsOrNull(bundleBasePath);
        if (scopedUserRoleAssignmentsXml == null || scopedUserRoleAssignmentsXml.getItems() == null || scopedUserRoleAssignmentsXml.getItems().isEmpty()) {
            log.info("IDM bootstrap bundle: scoped user-role assignments XML missing/empty (skipping scoped user-role assignments), basePath={}", bundleBasePath);
        } else {
            scopedUserRoleAssignmentsXml.getItems().forEach(this::ensureScopedUserRoleAssignment);
        }
    }

    private void ensureAdditionalUser(UsersXml.UserXmlItem xmlItem, boolean force) {
        String username = required(xmlItem.getUsername(), "users XML: username");
        String displayName = xmlItem.getDisplayName();
        String email = xmlItem.getEmail();
        String password = required(xmlItem.getPassword(), "users XML: password");

        ensureUser(username, displayName, email, password, force, "additional bundle user");
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
            log.info("Created ApplicationScope from bundle: id={}, applicationKey={}, stageKey={}",
                    saved.getId(), saved.getApplicationKey(), saved.getStageKey());
            return saved;
        }

        ApplicationScope existing = existingOpt.get();

        if (force) {
            String newDesc = xmlItem.getDescription();
            if (!Objects.equals(existing.getDescription(), newDesc)) {
                existing.setDescription(newDesc);
                ApplicationScope saved = applicationScopeEntityService.save(existing);
                log.info("Updated ApplicationScope from bundle (force): id={}, applicationKey={}, stageKey={}",
                        saved.getId(), saved.getApplicationKey(), saved.getStageKey());
                return saved;
            }
        }

        log.info("ApplicationScope from bundle exists: id={}, applicationKey={}, stageKey={}",
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
            log.info("Created scoped Role from bundle: id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
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
                log.info("Updated scoped Role from bundle (force): id={}, scopeId={}, name={}", saved.getId(), scope.getId(), saved.getName());
                return saved;
            }
        }

        log.info("Scoped Role from bundle exists: id={}, scopeId={}, name={}", existing.getId(), scope.getId(), existing.getName());
        return existing;
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

        boolean exists = assignmentEntityService.existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId());

        if (exists) {
            log.info("User-application-scope assignment from bundle exists: username={}, applicationKey={}, stageKey={}",
                    user.getUsername(), scope.getApplicationKey(), scope.getStageKey());
            return;
        }

        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(scope);

        UserApplicationScopeAssignment saved = assignmentEntityService.save(assignment);
        log.info("Created user-application-scope assignment from bundle: id={}, username={}, applicationKey={}, stageKey={}",
                saved.getId(), user.getUsername(), scope.getApplicationKey(), scope.getStageKey());
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
            log.info("Scoped UserRoleAssignment from bundle exists: username={}, applicationKey={}, stageKey={}, roleName={}",
                    username, applicationKey, stageKey, roleName);
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUserAccount(user);
        assignment.setRole(role);

        UserRoleAssignment saved = userRoleAssignmentEntityService.save(assignment);
        log.info("Created scoped UserRoleAssignment from bundle: id={}, username={}, applicationKey={}, stageKey={}, roleName={}",
                saved.getId(), username, applicationKey, stageKey, roleName);
    }

    private Optional<String> optionalTrim(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
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