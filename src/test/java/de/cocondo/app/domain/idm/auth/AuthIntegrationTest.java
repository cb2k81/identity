package de.cocondo.app.domain.idm.auth;

import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignment;
import de.cocondo.app.domain.idm.assignment.UserApplicationScopeAssignmentEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccount;
import de.cocondo.app.domain.idm.user.UserAccountEntityService;
import de.cocondo.app.domain.idm.user.UserAccountDomainService;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.system.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "secret";
    private static final String APPLICATION_KEY = "IDM";
    private static final String STAGE_KEY = "TEST";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserAccountDomainService userAccountDomainService;

    @Autowired
    private UserAccountEntityService userAccountEntityService;

    @Autowired
    private ApplicationScopeEntityService applicationScopeEntityService;

    @Autowired
    private UserApplicationScopeAssignmentEntityService userScopeAssignmentService;

    private ApplicationScope scope;
    private UserAccount user;

    @BeforeEach
    void setup() {

        // 1) Scope sicherstellen
        scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(APPLICATION_KEY, STAGE_KEY)
                .orElseGet(() -> {
                    ApplicationScope s = new ApplicationScope();
                    s.setApplicationKey(APPLICATION_KEY);
                    s.setStageKey(STAGE_KEY);
                    s.setDescription("Test Scope");
                    return applicationScopeEntityService.save(s);
                });

        // 2) User sicherstellen
        try {
            CreateUserRequestDTO dto = new CreateUserRequestDTO();
            dto.setUsername(TEST_USERNAME);
            dto.setPassword(TEST_PASSWORD);
            userAccountDomainService.createUser(dto);
        } catch (IllegalArgumentException ignored) {
            // user already exists
        }

        user = userAccountEntityService
                .loadByUsername(TEST_USERNAME)
                .orElseThrow();

        // 3) Scope-Zuordnung sicherstellen
        if (!userScopeAssignmentService
                .existsByUserAccountIdAndApplicationScopeId(user.getId(), scope.getId())) {

            UserApplicationScopeAssignment assignment = new UserApplicationScopeAssignment();
            assignment.setUserAccount(user);
            assignment.setApplicationScope(scope);
            userScopeAssignmentService.save(assignment);
        }
    }

    private String loginJson(String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s",
                    "applicationKey": "%s",
                    "stageKey": "%s"
                }
                """.formatted(TEST_USERNAME, password, APPLICATION_KEY, STAGE_KEY);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() throws Exception {

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.expiresAt", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse
                .split("\\\"token\\\":\\\"")[1]
                .split("\\\"")[0];

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.get("sub", String.class)).isEqualTo(user.getId());
        assertThat(claims.get("username", String.class)).isEqualTo(TEST_USERNAME);
        assertThat(claims.get("applicationKey", String.class)).isEqualTo(APPLICATION_KEY);
        assertThat(claims.get("stageKey", String.class)).isEqualTo(STAGE_KEY);

        Object roles = claims.get("roles");
        assertThat(roles).isInstanceOf(List.class);
        assertThat((List<?>) roles).isEmpty();
    }

    @Test
    void login_shouldReturn401_whenPasswordInvalid() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn401_whenNoToken() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn200_whenTokenValid() throws Exception {

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse
                .split("\"token\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));
    }
}