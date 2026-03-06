package de.cocondo.app.domain.idm.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.bootstrap.base-path=idm/bootstrap",

        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST",

        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRoleAssignmentIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${idm.bootstrap.admin.username}")
    private String adminUsername;

    @Value("${idm.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${idm.self.scope.application-key}")
    private String applicationKey;

    @Value("${idm.self.scope.stage-key}")
    private String stageKey;

    @Test
    void assign_role_to_user_as_admin() throws Exception {

        String token = loginAndGetToken();

        // scopeId via scopes endpoint
        String scopesBody = mvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String scopeId = objectMapper.readTree(scopesBody).get(0).get("id").asText();

        // create role
        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scopeId);
        roleReq.setName("R_TEST");
        roleReq.setDescription("test role");
        roleReq.setSystemProtected(false);

        String roleBody = mvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_TEST")))
                .andReturn().getResponse().getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        // create user
        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_assign");
        userReq.setPassword("pw");

        String userBody = mvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_assign")))
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(userBody).get("id").asText();

        // assign role to user
        var assignReq = new de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO();
        assignReq.setUserAccountId(userId);
        assignReq.setRoleId(roleId);

        mvc.perform(post("/api/idm/assignments/user-role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken() throws Exception {

        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername(adminUsername);
        req.setPassword(adminPassword);
        req.setApplicationKey(applicationKey);
        req.setStageKey(stageKey);

        String body = mvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}