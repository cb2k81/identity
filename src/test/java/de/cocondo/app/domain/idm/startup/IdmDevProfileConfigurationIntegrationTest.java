package de.cocondo.app.domain.idm.startup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:idm-devtest;DB_CLOSE_DELAY=-1;MODE=MariaDB",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("dev")
class IdmDevProfileConfigurationIntegrationTest {

    @Autowired
    private IdmBootstrapProperties bootstrapProperties;

    @Autowired
    private IdmSelfProperties selfProperties;

    @Autowired
    private Environment environment;

    @Test
    void configuration_shouldMatch_application_dev_yml() {

        assertThat(bootstrapProperties.isEnabled()).isTrue();
        assertThat(bootstrapProperties.getMode()).isEqualTo("safe");
        assertThat(bootstrapProperties.getBasePath()).isEqualTo("idm/bootstrap");

        assertThat(bootstrapProperties.getAdminXml()).isEqualTo("admin-user.xml");
        assertThat(bootstrapProperties.getScopesXml()).isEqualTo("scopes.xml");
        assertThat(bootstrapProperties.getPermissionGroupsXml()).isEqualTo("permission-groups.xml");
        assertThat(bootstrapProperties.getPermissionsXml()).isEqualTo("permissions.xml");
        assertThat(bootstrapProperties.getRolesXml()).isEqualTo("roles.xml");
        assertThat(bootstrapProperties.getRolePermissionAssignmentsXml()).isEqualTo("role-permission-assignments.xml");
        assertThat(bootstrapProperties.getUserRoleAssignmentsXml()).isEqualTo("user-role-assignments.xml");

        assertThat(bootstrapProperties.getAdmin().getUsername()).isEqualTo("admin");
        assertThat(bootstrapProperties.getAdmin().getPassword()).isNotBlank();

        String minimumLengthProperty = environment.getProperty("idm.security.password-policy.minimum-length");
        assertThat(minimumLengthProperty).isNotBlank();

        int minimumLength = Integer.parseInt(minimumLengthProperty);
        assertThat(minimumLength).isGreaterThan(0);

        assertThat(bootstrapProperties.getAdmin().getPassword().length()).isGreaterThanOrEqualTo(minimumLength);

        assertThat(selfProperties.getScope().getApplicationKey()).isEqualTo("IDM");
        assertThat(selfProperties.getScope().getStageKey()).isEqualTo("DEV");
    }
}