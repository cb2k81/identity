package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.assignment.dto.AssignPermissionToRoleRequestDTO;
import de.cocondo.app.domain.idm.permission.Permission;
import de.cocondo.app.domain.idm.permission.PermissionEntityService;
import de.cocondo.app.domain.idm.permission.PermissionGroup;
import de.cocondo.app.domain.idm.permission.PermissionGroupEntityService;
import de.cocondo.app.domain.idm.role.dto.CreateRoleRequestDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeRepository;
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

class RolePermissionAssignmentDuplicateIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private PermissionGroupEntityService permissionGroupEntityService;

    @Autowired
    private PermissionEntityService permissionEntityService;

    @Autowired
    private RolePermissionAssignmentRepository rolePermissionAssignmentRepository;

    @Test
    void assigning_same_permission_to_same_role_twice_returns_server_error_and_does_not_create_duplicate() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        // create role via API
        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scope.getId());
        roleReq.setName("R_ROLE_PERMISSION_DUP_TEST");
        roleReq.setDescription("role for duplicate role-permission assignment test");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_ROLE_PERMISSION_DUP_TEST")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        // create permission group via EntityService (deterministic setup without assuming controller path)
        PermissionGroup group = new PermissionGroup();
        group.setApplicationScope(scope);
        group.setName("PG_ROLE_PERMISSION_DUP_TEST");
        group.setDescription("permission group for duplicate role-permission assignment test");

        PermissionGroup savedGroup = permissionGroupEntityService.save(group);

        // create permission via EntityService (same reason: no unverified controller path assumptions)
        Permission permission = new Permission();
        permission.setApplicationScope(scope);
        permission.setPermissionGroup(savedGroup);
        permission.setName("P_ROLE_PERMISSION_DUP_TEST");
        permission.setDescription("permission for duplicate role-permission assignment test");
        permission.setSystemProtected(false);

        Permission savedPermission = permissionEntityService.save(permission);

        String permissionId = savedPermission.getId();

        AssignPermissionToRoleRequestDTO request = new AssignPermissionToRoleRequestDTO();
        request.setRoleId(roleId);
        request.setPermissionId(permissionId);

        // first assignment succeeds
        mockMvc.perform(post("/api/idm/assignments/role-permission")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // current MVP state:
        // duplicate protection is enforced by the database unique constraint.
        // the API currently returns a server error instead of a domain-specific conflict response.
        mockMvc.perform(post("/api/idm/assignments/role-permission")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        long matchingAssignments = rolePermissionAssignmentRepository.findAllByRole_Id(roleId)
                .stream()
                .filter(assignment -> assignment.getPermission().getId().equals(permissionId))
                .count();

        assertThat(matchingAssignments).isEqualTo(1L);
    }
}