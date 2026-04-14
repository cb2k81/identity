package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
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

        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST",

        "idm.security.login-protection.max-failed-attempts=3",
        "idm.security.login-protection.lock-duration-seconds=1",

        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoginProtectionIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    private static final String BOOTSTRAP_ADMIN_USERNAME = "admin";
    private static final String BOOTSTRAP_ADMIN_PASSWORD = "secret";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${idm.self.scope.application-key}")
    String applicationKey;

    @Value("${idm.self.scope.stage-key}")
    String stageKey;

    @Test
    void account_locked_after_multiple_failed_logins() throws Exception {

        LoginRequestDTO wrong = new LoginRequestDTO();
        wrong.setUsername(BOOTSTRAP_ADMIN_USERNAME);
        wrong.setPassword("wrong-password");
        wrong.setApplicationKey(applicationKey);
        wrong.setStageKey(stageKey);

        for (int i = 0; i < 3; i++) {

            mvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrong)))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrong)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_possible_again_after_lock_expires() throws Exception {

        LoginRequestDTO wrong = new LoginRequestDTO();
        wrong.setUsername(BOOTSTRAP_ADMIN_USERNAME);
        wrong.setPassword("wrong-password");
        wrong.setApplicationKey(applicationKey);
        wrong.setStageKey(stageKey);

        for (int i = 0; i < 3; i++) {

            mvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrong)))
                    .andExpect(status().isUnauthorized());
        }

        Thread.sleep(1500);

        LoginRequestDTO correct = new LoginRequestDTO();
        correct.setUsername(BOOTSTRAP_ADMIN_USERNAME);
        correct.setPassword(BOOTSTRAP_ADMIN_PASSWORD);
        correct.setApplicationKey(applicationKey);
        correct.setStageKey(stageKey);

        mvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshExpiresAt").exists());
    }
}