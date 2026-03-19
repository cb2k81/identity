package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.UserAccountState;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleUsersInScopeQueryIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Test
    void list_users_of_role_in_scope_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scope.getId());
        roleReq.setName("R_USERS_IN_SCOPE");
        roleReq.setDescription("role for scoped role-user query");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_USERS_IN_SCOPE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_role_users");
        userReq.setDisplayName("User Role Users");
        userReq.setEmail("u_role_users@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_role_users")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(userBody).get("id").asText();

        AssignApplicationScopeToUserRequestDTO scopeAssignReq = new AssignApplicationScopeToUserRequestDTO();
        scopeAssignReq.setUserAccountId(userId);
        scopeAssignReq.setApplicationScopeId(scope.getId());

        mockMvc.perform(post("/api/idm/assignments/user-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeAssignReq)))
                .andExpect(status().isOk());

        AssignRoleToUserRequestDTO roleAssignReq = new AssignRoleToUserRequestDTO();
        roleAssignReq.setUserAccountId(userId);
        roleAssignReq.setRoleId(roleId);

        mockMvc.perform(post("/api/idm/assignments/user-role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleAssignReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/idm/assignments/user-role/roles/" + roleId + "/users")
                        .header("Authorization", "Bearer " + token)
                        .param("applicationKey", applicationKey)
                        .param("stageKey", stageKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userId)))
                .andExpect(jsonPath("$[0].username", is("u_role_users")))
                .andExpect(jsonPath("$[0].displayName", is("User Role Users")))
                .andExpect(jsonPath("$[0].email", is("u_role_users@test.local")))
                .andExpect(jsonPath("$[0].state", is(UserAccountState.ACTIVE.name())));
    }
}