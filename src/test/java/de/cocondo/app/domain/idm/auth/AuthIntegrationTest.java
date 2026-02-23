package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.user.UserAccountDomainService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountDomainService userAccountDomainService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_USERNAME = "admin";
    private final String TEST_PASSWORD = "secret";

    @BeforeEach
    void setup() {
        try {
            userAccountDomainService.createUser(TEST_USERNAME, TEST_PASSWORD);
        } catch (IllegalArgumentException ignored) {
            // user already exists (test isolation minimal MVP)
        }
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() throws Exception {

        String requestBody = """
                {
                    "username": "admin",
                    "password": "secret"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt", notNullValue()));
    }

    @Test
    void login_shouldReturn401_whenPasswordInvalid() throws Exception {

        String requestBody = """
                {
                    "username": "admin",
                    "password": "wrong"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void me_shouldReturn401_whenNoToken() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn200_whenTokenValid() throws Exception {

        String loginRequest = """
                {
                    "username": "admin",
                    "password": "secret"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse
                .split("\"token\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }
}