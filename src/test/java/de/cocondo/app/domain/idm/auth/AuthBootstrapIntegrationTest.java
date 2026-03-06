package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.admin.password=admin"
})
class AuthBootstrapIntegrationTest {

    // -------------------------------------------------------------------------
    // Test Contracts (zentral in der Testklasse definiert)
    // -------------------------------------------------------------------------

    private static final String LOGIN_ENDPOINT = "/auth/login";

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private static final String SELF_APPLICATION_KEY = "IDM";
    private static final String SELF_STAGE_KEY = "TEST";

    // -------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private LoginRequestDTO loginRequest(
            String username,
            String password,
            String applicationKey,
            String stageKey
    ) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);
        request.setApplicationKey(applicationKey);
        request.setStageKey(stageKey);
        return request;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void login_shouldWork_withBootstrapAdmin_andSelfScope() throws Exception {

        LoginRequestDTO request = loginRequest(
                ADMIN_USERNAME,
                ADMIN_PASSWORD,
                SELF_APPLICATION_KEY,
                SELF_STAGE_KEY
        );

        mockMvc.perform(
                        post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void login_shouldFail_withWrongCredentials() throws Exception {

        LoginRequestDTO request = loginRequest(
                ADMIN_USERNAME,
                "wrong",
                SELF_APPLICATION_KEY,
                SELF_STAGE_KEY
        );

        mockMvc.perform(
                        post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_shouldFail_withWrongScope() throws Exception {

        LoginRequestDTO request = loginRequest(
                ADMIN_USERNAME,
                ADMIN_PASSWORD,
                SELF_APPLICATION_KEY,
                "DEV"
        );

        mockMvc.perform(
                        post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}