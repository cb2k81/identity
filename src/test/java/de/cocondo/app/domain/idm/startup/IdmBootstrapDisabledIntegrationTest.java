package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentRepository;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IdmBootstrapDisabledIntegrationTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private UserApplicationScopeAssignmentRepository assignmentRepository;

    @Test
    void bootstrapDisabled_shouldNotCreateAnyBootstrapData() {
        // application-test.yml has bootstrap disabled by default (or at least not enabled explicitly)
        assertThat(userAccountRepository.findByUsername("admin")).isEmpty();
        assertThat(applicationScopeRepository.findByApplicationKeyAndStageKey("IDM", "TEST")).isEmpty();
        assertThat(assignmentRepository.count()).isEqualTo(0L);
    }
}