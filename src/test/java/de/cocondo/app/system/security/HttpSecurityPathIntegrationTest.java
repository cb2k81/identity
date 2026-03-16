package de.cocondo.app.system.security;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the global path security rules:
 *
 * - /public/** is reachable anonymously (200) and also with token (200)
 * - /api/** requires authentication:
 *   - no token => 401
 *   - valid token => 200
 *
 * NOTE:
 * Testdaten werden ausschließlich über EntityServices erzeugt.
 * DomainServices werden hier NICHT verwendet (Method Security).
 */
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
public class HttpSecurityPathIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "secret";
    private static final String APPLICATION_KEY = "IDM";
    private static final String STAGE_KEY = "TEST";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void setUp() {
        testScope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(APPLICATION_KEY, STAGE_KEY)
                .orElseThrow(() -> new IllegalStateException(
                        "Scope " + APPLICATION_KEY + "/" + STAGE_KEY + " not bootstrapped"
                ));
    }

    @Test
    void noToken_publicIs200() throws Exception {
        mockMvc.perform(get("/public/application/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId", notNullValue()));
    }

    @Test
    void noToken_apiIs401() throws Exception {
        mockMvc.perform(get("/api/application/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void token_publicIs200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/public/application/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId", notNullValue()));
    }

    @Test
    void token_apiIs200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/application/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId", notNullValue()));
    }

    @Test
    void userWithScopeButNoRole_getsTokenAndCanAccessAuthenticatedApi() throws Exception {

        UserAccount user = new UserAccount();
        user.setUsername("httpPathPlainUser");
        user.setDisplayName("HTTP Path Plain User");
        user.setEmail("http-path-plain-user@test.local");
        user.setPasswordHash(passwordEncoder.encode("secret"));
        user.activate();
        userAccountEntityService.save(user);

        UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
        assignment.setUserAccount(user);
        assignment.setApplicationScope(testScope);
        userApplicationScopeAssignmentEntityService.save(assignment);

        String loginRequest = """
                {
                    "username": "httpPathPlainUser",
                    "password": "secret",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(APPLICATION_KEY, STAGE_KEY);

        String loginResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponseBody).get("token").asText();

        mockMvc.perform(get("/api/application/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId", notNullValue()));
    }

    private String loginAndGetToken() throws Exception {

        String loginRequest = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(TEST_USERNAME, TEST_PASSWORD, APPLICATION_KEY, STAGE_KEY);

        String loginResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(loginResponseBody);
        return root.get("token").asText();
    }
}