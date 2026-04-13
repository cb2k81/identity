package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.assignment.dto.AssignRoleToUserRequestDTO;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRolesInScopeQueryIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Test
    void list_roles_of_user_in_scope_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scope.getId());
        roleReq.setName("R_SCOPE_QUERY");
        roleReq.setDescription("role for scoped user-role query");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_SCOPE_QUERY")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_roles");
        userReq.setDisplayName("User Scope Roles");
        userReq.setEmail("u_scope_roles@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_roles")))
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

        mockMvc.perform(get("/api/idm/assignments/user-role/users/" + userId + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .param("applicationKey", applicationKey)
                        .param("stageKey", stageKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(roleId)))
                .andExpect(jsonPath("$[0].applicationScopeId", is(scope.getId())))
                .andExpect(jsonPath("$[0].name", is("R_SCOPE_QUERY")))
                .andExpect(jsonPath("$[0].description", is("role for scoped user-role query")))
                .andExpect(jsonPath("$[0].systemProtected", is(false)));
    }

    @Test
    void list_roles_of_user_in_scope_paged_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scope.getId());
        roleReq.setName("R_SCOPE_QUERY_PAGED");
        roleReq.setDescription("role for scoped user-role paged query");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_SCOPE_QUERY_PAGED")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_roles_paged");
        userReq.setDisplayName("User Scope Roles Paged");
        userReq.setEmail("u_scope_roles_paged@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_roles_paged")))
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

        mockMvc.perform(get("/api/idm/assignments/user-role/users/{userAccountId}/roles/list", userId)
                        .header("Authorization", "Bearer " + token)
                        .param("applicationKey", applicationKey)
                        .param("stageKey", stageKey)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(roleId)))
                .andExpect(jsonPath("$.items[0].applicationScopeId", is(scope.getId())))
                .andExpect(jsonPath("$.items[0].name", is("R_SCOPE_QUERY_PAGED")))
                .andExpect(jsonPath("$.items[0].description", is("role for scoped user-role paged query")))
                .andExpect(jsonPath("$.items[0].systemProtected", is(false)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
}