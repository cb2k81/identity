package de.cocondo.app.domain.idm.auth;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST"
})
class IdmAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    @Autowired
    private UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ApplicationScope testScope;

    @BeforeEach
    void resolveScope() {
        testScope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey("IDM", "TEST")
                .orElseThrow(() -> new IllegalStateException("Scope IDM/TEST not bootstrapped"));
    }

    private String login(String username, String password) throws Exception {

        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "IDM",
                    "stageKey": "TEST"
                }
                """.formatted(username, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.split("\"token\":\"")[1].split("\"")[0];
    }

    @Test
    void noToken_shouldReturn401() throws Exception {

        mockMvc.perform(get("/api/idm/scopes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutScope_shouldReturn401() throws Exception {

        UserAccount user = new UserAccount();
        user.setUsername("noScopeUser");
        user.setPasswordHash(passwordEncoder.encode("secret"));
        user.activate();
        userAccountEntityService.save(user);

        String loginJson = """
                {
                    "username": "noScopeUser",
                    "password": "secret",
                    "applicationKey": "IDM",
                    "stageKey": "TEST"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userWithScopeButNoRole_shouldReturn403() throws Exception {

        UserAccount user = new UserAccount();
        user.setUsername("plainUser");
        user.setPasswordHash(passwordEncoder.encode("secret"));
        user.activate();
        userAccountEntityService.save(user);

        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(testScope);
        userApplicationScopeAssignmentEntityService.save(assignment);

        String token = login("plainUser", "secret");

        mockMvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldAccessProtectedEndpoint() throws Exception {

        String token = login("admin", "admin");

        mockMvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}