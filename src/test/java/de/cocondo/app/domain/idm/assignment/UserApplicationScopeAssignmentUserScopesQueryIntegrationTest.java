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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApplicationScopeAssignmentUserScopesQueryIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Test
    void list_scopes_of_user_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_query");
        userReq.setDisplayName("User Scope Query");
        userReq.setEmail("u_scope_query@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_query")))
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

        mockMvc.perform(get("/api/idm/assignments/user-scope/users/{userAccountId}/scopes", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(scope.getId())))
                .andExpect(jsonPath("$[0].applicationKey", is(scope.getApplicationKey())))
                .andExpect(jsonPath("$[0].stageKey", is(scope.getStageKey())));
    }

    @Test
    void list_scopes_of_user_paged_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_query_paged");
        userReq.setDisplayName("User Scope Query Paged");
        userReq.setEmail("u_scope_query_paged@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_query_paged")))
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

        mockMvc.perform(get("/api/idm/assignments/user-scope/users/{userAccountId}/scopes/list", userId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "applicationKey")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(scope.getId())))
                .andExpect(jsonPath("$.items[0].applicationKey", is(scope.getApplicationKey())))
                .andExpect(jsonPath("$.items[0].stageKey", is(scope.getStageKey())))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
}