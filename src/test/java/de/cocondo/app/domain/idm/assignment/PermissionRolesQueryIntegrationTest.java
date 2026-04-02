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

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermissionRolesQueryIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private ApplicationScopeRepository applicationScopeRepository;

    @Autowired
    private PermissionGroupEntityService permissionGroupEntityService;

    @Autowired
    private PermissionEntityService permissionEntityService;

    @Test
    void list_roles_of_permission_as_admin() throws Exception {

        String token = loginAdminAndGetToken();

        ApplicationScope scope = applicationScopeRepository
                .findByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow();

        CreateRoleRequestDTO roleReq = new CreateRoleRequestDTO();
        roleReq.setApplicationScopeId(scope.getId());
        roleReq.setName("R_PERMISSION_ROLE_QUERY");
        roleReq.setDescription("role for permission-role query test");
        roleReq.setSystemProtected(false);

        String roleBody = mockMvc.perform(post("/api/idm/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name", is("R_PERMISSION_ROLE_QUERY")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String roleId = objectMapper.readTree(roleBody).get("id").asText();

        PermissionGroup group = new PermissionGroup();
        group.setApplicationScope(scope);
        group.setName("PG_PERMISSION_ROLE_QUERY");
        group.setDescription("permission group for permission-role query test");

        PermissionGroup savedGroup = permissionGroupEntityService.save(group);

        Permission permission = new Permission();
        permission.setApplicationScope(scope);
        permission.setPermissionGroup(savedGroup);
        permission.setName("P_PERMISSION_ROLE_QUERY");
        permission.setDescription("permission for permission-role query test");
        permission.setSystemProtected(false);

        Permission savedPermission = permissionEntityService.save(permission);

        AssignPermissionToRoleRequestDTO request = new AssignPermissionToRoleRequestDTO();
        request.setRoleId(roleId);
        request.setPermissionId(savedPermission.getId());

        mockMvc.perform(post("/api/idm/assignments/role-permission")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/idm/assignments/role-permission/permissions/{permissionId}/roles", savedPermission.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(roleId)))
                .andExpect(jsonPath("$[0].applicationScopeId", is(scope.getId())))
                .andExpect(jsonPath("$[0].name", is("R_PERMISSION_ROLE_QUERY")))
                .andExpect(jsonPath("$[0].description", is("role for permission-role query test")))
                .andExpect(jsonPath("$[0].systemProtected", is(false)));
    }
}