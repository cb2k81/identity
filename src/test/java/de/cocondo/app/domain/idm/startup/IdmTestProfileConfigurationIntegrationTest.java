package de.cocondo.app.domain.idm.startup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class IdmTestProfileConfigurationIntegrationTest {

    @Autowired
    private IdmBootstrapProperties bootstrapProperties;

    @Autowired
    private IdmSelfProperties selfProperties;

    @Autowired
    private Environment environment;

    @Test
    void bootstrap_configuration_contract_should_be_valid() {

        assertThat(bootstrapProperties.isEnabled()).isTrue();
        assertThat(bootstrapProperties.getMode()).isEqualTo("safe");

        assertThat(bootstrapProperties.getBasePath()).isEqualTo("idm/bootstrap-test");

        assertThat(bootstrapProperties.getAdminXml()).isNotBlank();
        assertThat(bootstrapProperties.getScopesXml()).isNotBlank();
        assertThat(bootstrapProperties.getPermissionGroupsXml()).isNotBlank();
        assertThat(bootstrapProperties.getPermissionsXml()).isNotBlank();
        assertThat(bootstrapProperties.getRolesXml()).isNotBlank();
        assertThat(bootstrapProperties.getRolePermissionAssignmentsXml()).isNotBlank();
        assertThat(bootstrapProperties.getUserRoleAssignmentsXml()).isNotBlank();
    }

    @Test
    void self_scope_contract_should_be_valid() {

        assertThat(selfProperties.getScope()).isNotNull();
        assertThat(selfProperties.getScope().getApplicationKey()).isNotBlank();
        assertThat(selfProperties.getScope().getStageKey()).isNotBlank();
    }

    @Test
    void jwt_configuration_contract_should_be_valid() {

        String secret = environment.getProperty("idm.security.jwt.secret");
        String ttl = environment.getProperty("idm.security.jwt.ttl-ms");

        assertThat(secret).isNotBlank();
        assertThat(secret.length()).isGreaterThanOrEqualTo(32);

        assertThat(ttl).isNotBlank();
        assertThat(Long.parseLong(ttl)).isGreaterThan(0);
    }
}