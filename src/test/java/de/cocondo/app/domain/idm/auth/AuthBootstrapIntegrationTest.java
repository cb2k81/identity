package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthBootstrapIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldWork_withBootstrapAdmin_and_selfScope() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("admin");
        request.setApplicationKey("IDM");
        request.setStageKey("TEST");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                // in eurem Response-Body ist es "expiresAt" (epoch millis)
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void login_shouldFail_withWrongCredentials() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("wrong");
        request.setApplicationKey("IDM");
        request.setStageKey("TEST");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_shouldFail_withWrongScope() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("admin");
        request.setApplicationKey("IDM");
        // "DEV" existiert im test-Kontext offenbar nicht/keine Assignment -> bei euch 401 (BadCredentialsException)
        request.setStageKey("DEV");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}