package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoleControllerIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Test
    void create_role_and_read_role() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope =
                applicationScopeRepository
                        .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                        .orElseThrow();

        CreateRoleRequestDTO request = new CreateRoleRequestDTO();
        request.setApplicationScopeId(scope.getId());
        request.setName("TEST_ROLE");
        request.setDescription("Test role");
        request.setSystemProtected(false);

        String createResponse =
                mockMvc.perform(post("/api/idm/roles")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name", is("TEST_ROLE")))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String roleId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/idm/roles/" + roleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(roleId)))
                .andExpect(jsonPath("$.name", is("TEST_ROLE")));
    }
}