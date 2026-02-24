package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignment;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountDomainService;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdmAuthorizationIntegrationTest {

    private static final String APPLICATION_KEY = "IDM";
    private static final String STAGE_KEY = "TEST";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountDomainService userAccountDomainService;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    @Autowired
    private UserApplicationScopeAssignmentEntityService userScopeAssignmentService;

    private ApplicationScope scope;

    @BeforeEach
    void setupScope() {

        scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(APPLICATION_KEY, STAGE_KEY)
                .orElseGet(() -> {
                    ApplicationScope s = new ApplicationScope();
                    s.setApplicationKey(APPLICATION_KEY);
                    s.setStageKey(STAGE_KEY);
                    s.setDescription("Test Scope");
                    return applicationScopeEntityService.save(s);
                });
    }

    private void ensureScopeAssignment(UserAccount user) {

        boolean exists = userScopeAssignmentService
                .existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId());

        if (!exists) {
            UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
            assignment.setUserAccount(user);
            assignment.setApplicationScope(scope);
            userScopeAssignmentService.save(assignment);
        }
    }

    private String login(String username, String password) throws Exception {

        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(username, password, APPLICATION_KEY, STAGE_KEY);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.split("\"token\":\"")[1].split("\"")[0];
    }

    @Test
    void admin_shouldAccessProtectedEndpoint() throws Exception {

        UserAccount admin = userAccountEntityService
                .loadByUsername("admin")
                .orElseThrow();

        ensureScopeAssignment(admin);

        String token = login("admin", "admin");

        mockMvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userWithoutRole_shouldReceive403() throws Exception {

        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername("plainUser");
        dto.setPassword("secret");
        userAccountDomainService.createUser(dto);

        UserAccount user = userAccountEntityService
                .loadByUsername("plainUser")
                .orElseThrow();

        ensureScopeAssignment(user);

        String token = login("plainUser", "secret");

        mockMvc.perform(get("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void noToken_shouldReceive401() throws Exception {

        mockMvc.perform(get("/api/idm/scopes"))
                .andExpect(status().isUnauthorized());
    }
}