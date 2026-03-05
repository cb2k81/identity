package de.cocondo.app.domain.idm.startup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IdmTestProfileConfigurationIntegrationTest {

    @Autowired
    private IdmBootstrapProperties bootstrapProperties;

    @Autowired
    private IdmSelfProperties selfProperties;

    @Test
    void configuration_shouldMatch_application_test_yml() {

        assertThat(bootstrapProperties.isEnabled()).isTrue();
        assertThat(bootstrapProperties.getMode()).isEqualTo("safe");
        assertThat(bootstrapProperties.getBasePath()).isEqualTo("idm/bootstrap");

        assertThat(bootstrapProperties.getAdminXml()).isEqualTo("admin.xml");
        assertThat(bootstrapProperties.getScopesXml()).isEqualTo("scopes.xml");
        assertThat(bootstrapProperties.getPermissionGroupsXml()).isEqualTo("permission-groups.xml");
        assertThat(bootstrapProperties.getPermissionsXml()).isEqualTo("permissions.xml");
        assertThat(bootstrapProperties.getRolesXml()).isEqualTo("roles.xml");
        assertThat(bootstrapProperties.getRolePermissionAssignmentsXml()).isEqualTo("role-permission-assignments.xml");
        assertThat(bootstrapProperties.getUserRoleAssignmentsXml()).isEqualTo("user-role-assignments.xml");

        assertThat(bootstrapProperties.getAdmin().getUsername()).isEqualTo("admin");
        assertThat(bootstrapProperties.getAdmin().getPassword()).isEqualTo("admin");

        assertThat(selfProperties.getScope().getApplicationKey()).isEqualTo("IDM");
        assertThat(selfProperties.getScope().getStageKey()).isEqualTo("TEST");
    }
}