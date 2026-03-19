package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroup;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class RolePermissionAssignmentCrossScopeIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    @Autowired
    private PermissionGroupEntityService permissionGroupEntityService;

    @Autowired
    private PermissionEntityService permissionEntityService;

    @Autowired
    private RolePermissionAssignmentRepository rolePermissionAssignmentRepository;

    @Test
    void assigning_permission_from_different_scope_to_role_returns_bad_request_and_does_not_persist() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scopeA = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        // create second scope via EntityService (deterministic setup, no unverified controller path assumptions)
        ApplicationScope scopeB = new ApplicationScope();
        scopeB.setApplicationKey("IDM_CROSS_SCOPE_TEST");
        scopeB.setStageKey("TEST");
        scopeB.setDescription("second scope for cross-scope assignment test");

        ApplicationScope savedScopeB = applicationScopeEntityService.save(scopeB);

        // create role in scope A via API
        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scopeA.getId());
        roleReq.setName("R_CROSS_SCOPE_TEST");
        roleReq.setDescription("role in scope A for cross-scope assignment test");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_CROSS_SCOPE_TEST")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        // create permission group in scope B
        PermissionGroup group = new PermissionGroup();
        group.setApplicationScope(savedScopeB);
        group.setName("PG_CROSS_SCOPE_TEST");
        group.setDescription("permission group in scope B for cross-scope assignment test");

        PermissionGroup savedGroup = permissionGroupEntityService.save(group);

        // create permission in scope B
        Permission permission = new Permission();
        permission.setApplicationScope(savedScopeB);
        permission.setPermissionGroup(savedGroup);
        permission.setName("P_CROSS_SCOPE_TEST");
        permission.setDescription("permission in scope B for cross-scope assignment test");
        permission.setSystemProtected(false);

        Permission savedPermission = permissionEntityService.save(permission);

        String permissionId = savedPermission.getId();

        AssignPermissionToRoleRequestDTO request = new AssignPermissionToRoleRequestDTO();
        request.setRoleId(roleId);
        request.setPermissionId(permissionId);

        mockMvc.perform(post("/api/idm/assignments/role-permission")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(
                rolePermissionAssignmentRepository.findAllByRole_Id(roleId)
                        .stream()
                        .noneMatch(assignment -> assignment.getPermission().getId().equals(permissionId))
        ).isTrue();
    }
}