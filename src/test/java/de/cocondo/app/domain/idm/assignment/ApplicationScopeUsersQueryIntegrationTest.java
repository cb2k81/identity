package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationScopeUsersQueryIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Test
    void list_users_of_scope_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_users");
        userReq.setDisplayName("User Scope Users");
        userReq.setEmail("u_scope_users@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_users")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(userBody).get("id").asText();

        AssignApplicationScopeToUserRequestDTO assignReq = new AssignApplicationScopeToUserRequestDTO();
        assignReq.setUserAccountId(userId);
        assignReq.setApplicationScopeId(scope.getId());

        mockMvc.perform(post("/api/idm/assignments/user-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/idm/assignments/user-scope/scopes/{applicationScopeId}/users", scope.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(userId)))
                .andExpect(jsonPath("$[*].username", hasItem("u_scope_users")))
                .andExpect(jsonPath("$[*].displayName", hasItem("User Scope Users")))
                .andExpect(jsonPath("$[*].email", hasItem("u_scope_users@test.local")))
                .andExpect(jsonPath("$[*].loginCount", hasItem(0)))
                .andExpect(jsonPath("$[*].lastLogin").isArray());
    }

    @Test
    void list_users_of_scope_paged_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_users_paged");
        userReq.setDisplayName("User Scope Users Paged");
        userReq.setEmail("u_scope_users_paged@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_users_paged")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(userBody).get("id").asText();

        AssignApplicationScopeToUserRequestDTO assignReq = new AssignApplicationScopeToUserRequestDTO();
        assignReq.setUserAccountId(userId);
        assignReq.setApplicationScopeId(scope.getId());

        mockMvc.perform(post("/api/idm/assignments/user-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/idm/assignments/user-scope/scopes/{applicationScopeId}/users/list", scope.getId())
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "username")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", hasItem(userId)))
                .andExpect(jsonPath("$.items[*].username", hasItem("u_scope_users_paged")))
                .andExpect(jsonPath("$.items[*].displayName", hasItem("User Scope Users Paged")))
                .andExpect(jsonPath("$.items[*].email", hasItem("u_scope_users_paged@test.local")))
                .andExpect(jsonPath("$.items[*].loginCount", hasItem(0)))
                .andExpect(jsonPath("$.items[*].lastLogin").isArray())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)));
    }
}