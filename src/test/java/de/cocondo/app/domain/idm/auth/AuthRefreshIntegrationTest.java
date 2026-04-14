package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.domain.idm.auth.dto.RefreshRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplicationRunner.class)
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
class AuthRefreshIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String REFRESH_ENDPOINT = "/auth/refresh";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void refresh_shouldIssueUsableAccessToken_andKeepRefreshContextStable() throws Exception {

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("secret");
        loginRequest.setApplicationKey("IDM");
        loginRequest.setStageKey("TEST");

        String loginResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshExpiresAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponseBody);
        String loginToken = loginJson.get("token").asText();
        String refreshToken = loginJson.get("refreshToken").asText();
        long loginRefreshExpiresAt = loginJson.get("refreshExpiresAt").asLong();

        RefreshRequestDTO refreshRequest = new RefreshRequestDTO();
        refreshRequest.setRefreshToken(refreshToken);

        String refreshResponseBody = mockMvc.perform(post(REFRESH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
                .andExpect(jsonPath("$.refreshExpiresAt").value(loginRefreshExpiresAt))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshJson = objectMapper.readTree(refreshResponseBody);
        String refreshedToken = refreshJson.get("token").asText();

        assertNotNull(loginToken);
        assertNotNull(refreshedToken);
        assertEquals(refreshToken, refreshJson.get("refreshToken").asText());
        assertEquals(loginRefreshExpiresAt, refreshJson.get("refreshExpiresAt").asLong());
    }

    @Test
    void refresh_withInvalidToken_mustReturnUnauthorized_withoutLeakingInformation() throws Exception {

        RefreshRequestDTO refreshRequest = new RefreshRequestDTO();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        mockMvc.perform(post(REFRESH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("org.springframework"))))
                .andExpect(content().string(not(containsString("IllegalArgumentException"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}