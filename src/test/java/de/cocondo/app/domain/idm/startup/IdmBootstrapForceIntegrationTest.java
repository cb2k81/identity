package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentRepository;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentRepository;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentRepository;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroup;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountDomainService;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Start with bootstrap OFF so we can set up "dirty" pre-state
        "idm.bootstrap.enabled=false",
        "idm.bootstrap.base-path=idm/bootstrap-test",
        "idm.bootstrap.admin-xml=admin-user-force.xml",
        "idm.bootstrap.scopes-xml=scopes-force.xml",

        "idm.bootstrap.permission-groups-xml=permission-groups-force.xml",
        "idm.bootstrap.permissions-xml=permissions-force.xml",
        "idm.bootstrap.roles-xml=roles-force.xml",
        "idm.bootstrap.role-permission-assignments-xml=role-permission-assignments-force.xml",
        "idm.bootstrap.user-role-assignments-xml=user-role-assignments-force.xml",

        "idm.self.application-key=IDM",
        "idm.self.stage-key=TEST"
})
class IdmBootstrapForceIntegrationTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private IdmBootstrapProperties bootstrapProperties;

    @Autowired
    private IdmBootstrapApplicationListener listener;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountDomainService userAccountDomainService;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService scopeEntityService;

    @Autowired
    private UserApplicationScopeAssignmentRepository assignmentRepository;

    @Autowired
    private PermissionGroupEntityService permissionGroupEntityService;

    @Autowired
    private PermissionEntityService permissionEntityService;

    @Autowired
    private RoleEntityService roleEntityService;

    @Autowired
    private RolePermissionAssignmentRepository rolePermissionAssignmentRepository;

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Test
    void bootstrapForce_shouldUpdateExistingScopeAndAdminPassword() {
        // --- Pre-state: create scope with wrong description and admin with wrong password
        ApplicationScope scope = new ApplicationScope();
        scope.setApplicationKey("IDM");
        scope.setStageKey("TEST");
        scope.setDescription("WRONG DESC");
        scopeEntityService.save(scope);

        CreateUserRequestDTO create = new CreateUserRequestDTO();
        create.setUsername("admin");
        create.setPassword("wrong");
        userAccountDomainService.createUser(create);

        UserAccount adminBefore = userAccountEntityService.loadByUsername("admin").orElseThrow();
        assertThat(passwordEncoder.matches("wrong", adminBefore.getPasswordHash())).isTrue();

        // Pre-state: create permission group/permission/role with wrong description and not protected
        PermissionGroup group = new PermissionGroup();
        group.setApplicationScope(scope);
        group.setName("IDM_PERMISSION");
        group.setDescription("WRONG GROUP DESC");
        permissionGroupEntityService.save(group);

        Permission perm = new Permission();
        perm.setApplicationScope(scope);
        perm.setPermissionGroup(group);
        perm.setName("IDM_SCOPE_READ");
        perm.setDescription("WRONG PERM DESC");
        perm.setSystemProtected(false);
        permissionEntityService.save(perm);

        Role role = new Role();
        role.setApplicationScope(scope);
        role.setName("IDM_ADMIN");
        role.setDescription("WRONG ROLE DESC");
        role.setSystemProtected(false);
        roleEntityService.save(role);

        // --- Enable bootstrap in FORCE mode and run listener manually
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setMode("force");

        ApplicationReadyEvent event =
                new ApplicationReadyEvent(new SpringApplication(), new String[0], context, Duration.ZERO);

        listener.onApplicationEvent(event);

        // --- Verify: updated password and scope description from FORCE xml
        UserAccount adminAfter = userAccountEntityService.loadByUsername("admin").orElseThrow();
        assertThat(passwordEncoder.matches("adminForce", adminAfter.getPasswordHash())).isTrue();
        assertThat(adminAfter.isActive()).isTrue();

        ApplicationScope scopeAfter = scopeEntityService
                .loadByApplicationKeyAndStageKey("IDM", "TEST")
                .orElseThrow();

        assertThat(scopeAfter.getDescription()).isEqualTo("IDM Test (FORCE override)");

        boolean assignmentExists =
                assignmentRepository.existsByUserAccount_IdAndApplicationScope_Id(adminAfter.getId(), scopeAfter.getId());

        assertThat(assignmentExists).isTrue();

        // --- Verify force updates for authz data
        PermissionGroup groupAfter = permissionGroupEntityService
                .loadByApplicationScopeIdAndName(scopeAfter.getId(), "IDM_PERMISSION")
                .orElseThrow();
        assertThat(groupAfter.getDescription()).isEqualTo("IDM Permission Group (FORCE override)");

        Permission permAfter = permissionEntityService
                .loadByApplicationScopeIdAndName(scopeAfter.getId(), "IDM_SCOPE_READ")
                .orElseThrow();
        assertThat(permAfter.getDescription()).isEqualTo("Read scopes (FORCE override)");
        assertThat(permAfter.isSystemProtected()).isTrue();

        Role roleAfter = roleEntityService
                .loadByApplicationScopeIdAndName(scopeAfter.getId(), "IDM_ADMIN")
                .orElseThrow();
        assertThat(roleAfter.getDescription()).isEqualTo("IDM Admin (FORCE override)");
        assertThat(roleAfter.isSystemProtected()).isTrue();

        assertThat(rolePermissionAssignmentRepository.count()).isEqualTo(1L);
        assertThat(userRoleAssignmentRepository.count()).isEqualTo(1L);
    }
}