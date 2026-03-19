package de.cocondo.app.domain.idm.startup;

import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=false"
})
class IdmBootstrapDisabledIntegrationTest {

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Test
    void bootstrapDisabled_shouldNotCreateAdmin() {

        assertThat(userAccountEntityService.loadByUsername("admin"))
                .isEmpty();
    }
}