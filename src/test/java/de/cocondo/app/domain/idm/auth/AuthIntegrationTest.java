package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignment;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    @Autowired
    private UserApplicationScopeAssignmentEntityService userScopeAssignmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String username;
    private String password;
    private String applicationKey;
    private String stageKey;

    @BeforeEach
    void setup() {

        username = "auth-user-" + UUID.randomUUID();
        password = "secret";

        applicationKey = "IDM-" + UUID.randomUUID();
        stageKey = "TEST-" + UUID.randomUUID();

        // Scope anlegen
        ApplicationScope scope = new ApplicationScope();
        scope.setApplicationKey(applicationKey);
        scope.setStageKey(stageKey);
        scope.setDescription("Integration Test Scope");
        scope = applicationScopeEntityService.save(scope);

        // User anlegen (EntityService, nicht DomainService!)
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.activate();
        user = userAccountEntityService.save(user);

        // Scope-Zuordnung
        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(scope);
        userScopeAssignmentService.save(assignment);
    }

    @Test
    void login_should_return_jwt_token() throws Exception {

        String loginRequest = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(username, password, applicationKey, stageKey);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_with_wrong_password_should_return_401() throws Exception {

        String loginRequest = """
                {
                    "username": "%s",
                    "password": "wrong",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(username, applicationKey, stageKey);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }
}