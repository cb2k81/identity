package de.cocondo.app.domain.idm.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.system.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.bootstrap.base-path=idm/bootstrap-personnel-test",
        "idm.bootstrap.admin-xml=admin-user.xml",
        "idm.bootstrap.users-xml=users.xml",
        "idm.bootstrap.scopes-xml=scopes.xml",
        "idm.bootstrap.user-application-scope-assignments-xml=user-application-scope-assignments.xml",
        "idm.bootstrap.scoped-roles-xml=scoped-roles.xml",
        "idm.bootstrap.scoped-user-role-assignments-xml=scoped-user-role-assignments.xml",
        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST"
})
class PersonnelLoginBootstrapIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    private static final String USERNAME = "personnel-app";
    private static final String PASSWORD = "personnel-secret";
    private static final String APPLICATION_KEY = "PERSONNEL";
    private static final String STAGE_KEY = "DEV";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void login_for_personnel_scope_returns_token_with_scope_specific_roles_only() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setApplicationKey(APPLICATION_KEY);
        request.setStageKey(STAGE_KEY);

        String responseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        String token = json.get("token").asText();
        JsonNode refreshTokenNode = json.get("refreshToken");
        JsonNode refreshExpiresAtNode = json.get("refreshExpiresAt");

        assertNotNull(refreshTokenNode);
        assertNotNull(refreshExpiresAtNode);

        Claims claims = jwtService.parseToken(token);

        assertEquals(USERNAME, claims.get("username", String.class));
        assertEquals(APPLICATION_KEY, claims.get("applicationKey", String.class));
        assertEquals(STAGE_KEY, claims.get("stageKey", String.class));

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        assertIterableEquals(List.of("PERSONNEL_APP"), roles);
    }
}