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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.bootstrap.base-path=idm/bootstrap",

        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST",

        "idm.security.login-protection.max-failed-attempts=3",
        "idm.security.login-protection.lock-duration-seconds=1",

        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoginProtectionIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${idm.bootstrap.admin.username}")
    String adminUsername;

    @Value("${idm.bootstrap.admin.password}")
    String adminPassword;

    @Value("${idm.self.scope.application-key}")
    String applicationKey;

    @Value("${idm.self.scope.stage-key}")
    String stageKey;

    @Test
    void account_locked_after_multiple_failed_logins() throws Exception {

        LoginRequestDTO wrong = new LoginRequestDTO();
        wrong.setUsername(adminUsername);
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
        wrong.setUsername(adminUsername);
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
        correct.setUsername(adminUsername);
        correct.setPassword(adminPassword);
        correct.setApplicationKey(applicationKey);
        correct.setStageKey(stageKey);

        mvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));
    }
}