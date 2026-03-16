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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
        "idm.self.scope.stage-key=TEST"
})
class IdmAuthorizationIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    private static final String BOOTSTRAP_ADMIN_USERNAME = "admin";
    private static final String BOOTSTRAP_ADMIN_PASSWORD = "secret";

    @Value("${idm.self.scope.application-key}")
    private String applicationKey;

    @Value("${idm.self.scope.stage-key}")
    private String stageKey;

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
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Scope " + applicationKey + "/" + stageKey + " not bootstrapped"
                ));
    }

    private String login(String username, String password) throws Exception {

        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(username, password, applicationKey, stageKey);

        String response = mockMvc.perform(post(LOGIN_ENDPOINT)
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
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(applicationKey, stageKey);

        mockMvc.perform(post(LOGIN_ENDPOINT)
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

        String token = login(BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_PASSWORD);

        mockMvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}