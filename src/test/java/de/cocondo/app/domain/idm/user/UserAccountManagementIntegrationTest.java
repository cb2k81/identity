package de.cocondo.app.domain.idm.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.MainApplicationRunner;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplicationRunner.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserAccountManagementIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/auth/login";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;

    private String scopeId;
    private String roleId;
    private String permissionId;
    private String adminUserId;

    @BeforeEach
    void setup() {
        scopeId = insertScope("IDM", "LOCAL");
        permissionId = insertPermission(scopeId, "IDM_USER_CREATE");
        roleId = insertRole(scopeId, "ADMIN");

        assignPermissionToRole(roleId, permissionId);

        adminUserId = insertUser("admin", "admin");

        assignRoleToUser(adminUserId, roleId);
        assignScopeToUser(adminUserId, scopeId);
    }

    @Test
    void create_user_as_admin() throws Exception {

        String token = loginAndGetToken("admin", "admin");

        CreateUserRequestDTO create = new CreateUserRequestDTO();
        create.setUsername("u1");
        create.setPassword("pw1");

        mvc.perform(post("/api/idm/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("u1")));
    }

    private String loginAndGetToken(String username, String password) throws Exception {

        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername(username);
        req.setPassword(password);
        req.setApplicationKey("IDM");
        req.setStageKey("LOCAL");

        String body = mvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    // -------------------------------------------------------------------------
    // INSERT HELPERS (unverändert aus Baseline)
    // -------------------------------------------------------------------------

    private String insertScope(String appKey, String stageKey) {
        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_application_scope
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, application_key, stage_key, description)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, id, now, null, now, null, 0, appKey, stageKey, "test");

        return id;
    }

    private String insertPermission(String scopeId, String name) {

        String groupId = UUID.randomUUID().toString();
        String permissionId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_permission_group
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, name, application_scope_id, description)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, groupId, now, null, now, null, 0, "DEFAULT", scopeId, "group");

        jdbc.update("""
            insert into idm_permission
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, name, application_scope_id,
             permission_group_id, description, system_protected)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, permissionId, now, null, now, null, 0,
                name, scopeId, groupId, "perm", false);

        return permissionId;
    }

    private String insertRole(String scopeId, String name) {

        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_role
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, name, application_scope_id,
             description, system_protected)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
                id,
                now, null,
                now, null,
                0,
                name,
                scopeId,
                "role",
                false
        );

        return id;
    }

    private void assignPermissionToRole(String roleId, String permissionId) {

        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_role_permission_assignment
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, role_id, permission_id)
            values (?, ?, ?, ?, ?, ?, ?, ?)
        """,
                id,
                now, null,
                now, null,
                0,
                roleId,
                permissionId
        );
    }

    private String insertUser(String username, String rawPassword) {

        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into user_account
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, username, password_hash, state)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
                id,
                now, null,
                now, null,
                0,
                username,
                passwordEncoder.encode(rawPassword),
                "ACTIVE"
        );

        return id;
    }

    private void assignRoleToUser(String userId, String roleId) {

        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_user_role_assignment
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, user_account_id, role_id)
            values (?, ?, ?, ?, ?, ?, ?, ?)
        """,
                id,
                now, null,
                now, null,
                0,
                userId,
                roleId
        );
    }

    private void assignScopeToUser(String userId, String scopeId) {

        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        jdbc.update("""
            insert into idm_user_application_scope_assignment
            (id, created_at, created_by, last_modified_at, last_modified_by,
             persistence_version, user_account_id, application_scope_id)
            values (?, ?, ?, ?, ?, ?, ?, ?)
        """,
                id,
                now, null,
                now, null,
                0,
                userId,
                scopeId
        );
    }
}