package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.assignment.RolePermissionAssignmentRepository;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentRepository;
import de.cocondo.app.domain.idm.assignment.UserRoleAssignmentRepository;
import de.cocondo.app.domain.idm.permission.PermissionGroupRepository;
import de.cocondo.app.domain.idm.permission.PermissionRepository;
import de.cocondo.app.domain.idm.role.RoleRepository;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.bootstrap.base-path=idm/bootstrap-test",
        "idm.bootstrap.admin-xml=admin-user.xml",
        "idm.bootstrap.scopes-xml=scopes.xml",
        "idm.bootstrap.permission-groups-xml=permission-groups.xml",
        "idm.bootstrap.permissions-xml=permissions.xml",
        "idm.bootstrap.roles-xml=roles.xml",
        "idm.bootstrap.role-permission-assignments-xml=role-permission-assignments.xml",
        "idm.bootstrap.user-role-assignments-xml=user-role-assignments.xml",
        "idm.self.application-key=IDM",
        "idm.self.stage-key=TEST"
})
class IdmBootstrapSafeIntegrationTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private UserApplicationScopeAssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionAssignmentRepository rolePermissionAssignmentRepository;

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Test
    void bootstrapSafe_shouldCreateScopeAdminAndAssignment() {

        UserAccount admin = userAccountRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.isActive()).isTrue();
        assertThat(passwordEncoder.matches("admin", admin.getPasswordHash())).isTrue();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey("IDM", "TEST")
                .orElseThrow();

        assertThat(scope.getDescription()).isEqualTo("IDM Test (from test resources)");

        boolean assignmentExists =
                assignmentRepository.existsByUserAccount_IdAndApplicationScope_Id(
                        admin.getId(),
                        scope.getId()
                );

        assertThat(assignmentExists).isTrue();

        // Authorization data (self-scope)
        assertThat(permissionGroupRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_PERMISSION")).isPresent();
        assertThat(permissionGroupRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_USER")).isPresent();

        assertThat(permissionRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_SCOPE_READ")).isPresent();
        assertThat(permissionRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_ROLE_CREATE")).isPresent();
        assertThat(permissionRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_USER_READ")).isPresent();

        assertThat(roleRepository.findByApplicationScope_IdAndName(scope.getId(), "IDM_ADMIN")).isPresent();

        assertThat(rolePermissionAssignmentRepository.count()).isEqualTo(2L);
        assertThat(userRoleAssignmentRepository.count()).isEqualTo(1L);
    }

    @Test
    void bootstrapSafe_shouldBeIdempotent() {

        assertThat(userAccountRepository.findByUsername("admin")).isPresent();
        assertThat(applicationScopeRepository.findByApplicationKeyAndStageKey("IDM", "TEST")).isPresent();

        // Baseline counts after single SAFE run
        assertThat(assignmentRepository.count()).isEqualTo(1L);

        assertThat(permissionGroupRepository.count()).isEqualTo(2L);
        assertThat(permissionRepository.count()).isEqualTo(3L);
        assertThat(roleRepository.count()).isEqualTo(1L);
        assertThat(rolePermissionAssignmentRepository.count()).isEqualTo(2L);
        assertThat(userRoleAssignmentRepository.count()).isEqualTo(1L);
    }
}