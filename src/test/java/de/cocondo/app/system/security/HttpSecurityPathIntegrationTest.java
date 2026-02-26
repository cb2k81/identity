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
public class HttpSecurityPathIntegrationTest {

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
    private UserApplicationScopeAssignmentEntityService userScopeAssignmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ApplicationScope scope;
    private UserAccount user;

    @BeforeEach
    void setup() {

        // 1) Scope sicherstellen
        scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(APPLICATION_KEY, STAGE_KEY)
                .orElseGet(() -> {
                    ApplicationScope s = new ApplicationScope();
                    s.setApplicationKey(APPLICATION_KEY);
                    s.setStageKey(STAGE_KEY);
                    s.setDescription("Test Scope");
                    return applicationScopeEntityService.save(s);
                });

        // 2) User sicherstellen (über EntityService, NICHT DomainService)
        user = userAccountEntityService
                .loadByUsername(TEST_USERNAME)
                .orElseGet(() -> {
                    UserAccount u = new UserAccount();
                    u.setUsername(TEST_USERNAME);
                    u.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
                    u.activate();
                    return userAccountEntityService.save(u);
                });

        // 3) Scope-Zuordnung sicherstellen
        if (!userScopeAssignmentService
                .existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId())) {

            UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
            assignment.setUserAccount(user);
            assignment.setApplicationScope(scope);
            userScopeAssignmentService.save(assignment);
        }
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

    private String loginAndGetToken() throws Exception {

        String loginRequest = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(TEST_USERNAME, TEST_PASSWORD, APPLICATION_KEY, STAGE_KEY);

        String loginResponseBody = mockMvc.perform(post("/api/auth/login")
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