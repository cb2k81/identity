package de.cocondo.app.system.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.user.UserAccountDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the global path security rules:
 * - /public/** is reachable anonymously (200) and also with token (200)
 * - /api/** requires authentication:
 *   - no token => 401
 *   - valid token => 200
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HttpSecurityPathIntegrationTest {

    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountDomainService userAccountDomainService;

    @BeforeEach
    void setup() {
        try {
            userAccountDomainService.createUser(TEST_USERNAME, TEST_PASSWORD);
        } catch (IllegalArgumentException ignored) {
            // user already exists (minimal test isolation)
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
                    "username": "admin",
                    "password": "secret"
                }
                """;

        String loginResponseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(loginResponseBody);
        return root.get("token").asText();
    }
}