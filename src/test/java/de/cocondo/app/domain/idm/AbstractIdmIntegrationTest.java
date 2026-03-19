package de.cocondo.app.domain.idm;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        "idm.self.scope.stage-key=TEST",

        // WICHTIG: kompatible Test-Properties für bestehende Unterklassen
        "idm.bootstrap.admin.username=admin",
        "idm.bootstrap.admin.password=secret"
})
public abstract class AbstractIdmIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Diese Felder bleiben bewusst erhalten,
     * damit bestehende Testklassen API-kompatibel bleiben.
     */
    @Value("${idm.bootstrap.admin.username}")
    protected String adminUsername;

    @Value("${idm.bootstrap.admin.password}")
    protected String adminPassword;

    @Value("${idm.self.scope.application-key}")
    protected String applicationKey;

    @Value("${idm.self.scope.stage-key}")
    protected String stageKey;

    protected String loginAdminAndGetToken() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(adminUsername);
        request.setPassword(adminPassword);
        request.setApplicationKey(applicationKey);
        request.setStageKey(stageKey);

        String response = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Früher Smoke-Guard:
        // Ein erfolgreicher Admin-Login allein reicht nicht aus.
        // Der Token muss im Self-Scope auch tatsächlich Management-Zugriff haben.
        mockMvc.perform(
                        get("/api/idm/roles")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        return token;
    }
}