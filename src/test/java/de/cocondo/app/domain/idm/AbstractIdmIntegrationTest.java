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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIdmIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

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

        return objectMapper.readTree(response).get("token").asText();
    }
}