package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignApplicationScopeToUserRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApplicationScopeAssignmentDuplicateIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private UserApplicationScopeAssignmentRepository userApplicationScopeAssignmentRepository;

    @Test
    void assigning_same_scope_to_same_user_twice_returns_conflict_and_does_not_create_duplicate() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        // create user
        CreateUserRequestDTO userReq = new CreateUserRequestDTO();
        userReq.setUsername("u_scope_duplicate");
        userReq.setDisplayName("User Scope Duplicate");
        userReq.setEmail("u_scope_duplicate@test.local");
        userReq.setPassword("password");

        String userBody = mockMvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.username", is("u_scope_duplicate")))
                .andExpect(jsonPath("$.displayName", is("User Scope Duplicate")))
                .andExpect(jsonPath("$.email", is("u_scope_duplicate@test.local")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(userBody).get("id").asText();

        AssignApplicationScopeToUserRequestDTO request = new AssignApplicationScopeToUserRequestDTO();
        request.setUserAccountId(userId);
        request.setApplicationScopeId(scope.getId());

        long totalAssignmentsBefore = userApplicationScopeAssignmentRepository.count();

        // first assignment
        mockMvc.perform(post("/api/idm/assignments/user-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(
                userApplicationScopeAssignmentRepository.existsByUserAccount_IdAndApplicationScope_Id(userId, scope.getId())
        ).isTrue();

        long totalAssignmentsAfterFirstAssign = userApplicationScopeAssignmentRepository.count();
        assertThat(totalAssignmentsAfterFirstAssign).isEqualTo(totalAssignmentsBefore + 1);

        // second identical assignment -> now handled as domain-level conflict before DB unique constraint
        mockMvc.perform(post("/api/idm/assignments/user-scope")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(
                userApplicationScopeAssignmentRepository.existsByUserAccount_IdAndApplicationScope_Id(userId, scope.getId())
        ).isTrue();

        long totalAssignmentsAfterSecondAssign = userApplicationScopeAssignmentRepository.count();
        assertThat(totalAssignmentsAfterSecondAssign).isEqualTo(totalAssignmentsAfterFirstAssign);
    }
}