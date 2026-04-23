// Datei: src/test/java/de/cocondo/app/domain/idm/user/UserAccountControllerIntegrationTest.java
package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAccountControllerIntegrationTest extends AbstractIdmIntegrationTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void create_user_and_read_user() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();
        String displayName = "User Display";
        String email = "user@example.org";

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setDisplayName(displayName);
        request.setEmail(email);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.displayName").value(displayName))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.loginCount").value(0))
                .andExpect(jsonPath("$.lastLogin").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        get("/api/idm/users/{id}", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.displayName").value(displayName))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.loginCount").value(0))
                .andExpect(jsonPath("$.lastLogin").doesNotExist());
    }

    @Test
    void read_admin_user_includes_login_metrics() throws Exception {

        String token = loginAdminAndGetToken();

        String listResponse = mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "1")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("username", adminUsername)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].username").value(adminUsername))
                .andExpect(jsonPath("$.items[0].loginCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.items[0].lastLogin").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String adminUserId = objectMapper.readTree(listResponse).get("items").get(0).get("id").asText();

        mockMvc.perform(
                        get("/api/idm/users/{id}", adminUserId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminUserId))
                .andExpect(jsonPath("$.username").value(adminUsername))
                .andExpect(jsonPath("$.loginCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.lastLogin").value(notNullValue()));
    }

    @Test
    void update_user_master_data() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO create = new CreateUserRequestDTO();
        create.setUsername(username);
        create.setDisplayName("User Display");
        create.setEmail("user@example.org");
        create.setPassword("password");

        String createResponse = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(create))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(createResponse).get("id").asText();

        UpdateUserRequestDTO update = new UpdateUserRequestDTO();
        update.setDisplayName("User Display Updated");
        update.setEmail("user.updated@example.org");

        mockMvc.perform(
                        put("/api/idm/users/{id}", userId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.displayName").value("User Display Updated"))
                .andExpect(jsonPath("$.email").value("user.updated@example.org"))
                .andExpect(jsonPath("$.state").value("ACTIVE"));

        mockMvc.perform(
                        get("/api/idm/users/{id}", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.displayName").value("User Display Updated"))
                .andExpect(jsonPath("$.email").value("user.updated@example.org"))
                .andExpect(jsonPath("$.state").value("ACTIVE"));
    }

    @Test
    void create_user_rejects_password_below_minimum_length() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setPassword("pw");

        mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password must have at least 3 characters"));
    }

    @Test
    void list_users_paged() throws Exception {

        String token = loginAdminAndGetToken();

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void change_password() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        ChangePasswordRequestDTO change = new ChangePasswordRequestDTO();
        change.setNewPassword("password2");

        mockMvc.perform(
                        put("/api/idm/users/{id}/password", userId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(change))
                )
                .andExpect(status().isOk());
    }

    @Test
    void change_password_rejects_password_below_minimum_length() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        ChangePasswordRequestDTO change = new ChangePasswordRequestDTO();
        change.setNewPassword("pw");

        mockMvc.perform(
                        put("/api/idm/users/{id}/password", userId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(change))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password must have at least 3 characters"));
    }

    @Test
    void activate_and_deactivate_user() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        put("/api/idm/users/{id}/deactivate", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DISABLED"));

        mockMvc.perform(
                        put("/api/idm/users/{id}/activate", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACTIVE"));
    }

    @Test
    void delete_user() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_" + UUID.randomUUID();

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        delete("/api/idm/users/{id}", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void list_users_exposes_failed_login_attempts_locked_until_and_last_modified_at() throws Exception {

        String token = loginAdminAndGetToken();

        String username = "user_list_fields_" + UUID.randomUUID();

        String userId = createUserViaApi(token, username, "User List Fields", "list-fields@example.org");

        UserAccount user = userAccountRepository.findById(userId).orElseThrow();
        Instant lockedUntil = Instant.parse("2030-01-01T10:15:30Z");
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(lockedUntil);
        UserAccount saved = userAccountRepository.save(user);

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("username", username)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(userId))
                .andExpect(jsonPath("$.items[0].username").value(username))
                .andExpect(jsonPath("$.items[0].failedLoginAttempts").value(3))
                .andExpect(jsonPath("$.items[0].lockedUntil").value(lockedUntil.toString()))
                .andExpect(jsonPath("$.items[0].lastModifiedAt").isNotEmpty());

        String listResponse = mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("username", username)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(userId))
                .andExpect(jsonPath("$.items[0].username").value(username))
                .andExpect(jsonPath("$.items[0].failedLoginAttempts").value(3))
                .andExpect(jsonPath("$.items[0].lockedUntil").value(lockedUntil.toString()))
                .andExpect(jsonPath("$.items[0].lastModifiedAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LocalDateTime responseLastModifiedAt = LocalDateTime.parse(
                objectMapper.readTree(listResponse).get("items").get(0).get("lastModifiedAt").asText()
        );

        org.junit.jupiter.api.Assertions.assertNotNull(saved.getLastModifiedAt());
        org.junit.jupiter.api.Assertions.assertEquals(
                saved.getLastModifiedAt().withNano(0),
                responseLastModifiedAt.withNano(0)
        );
    }

    @Test
    void list_users_filters_by_failed_login_attempts() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_filter_failed_a_" + UUID.randomUUID();
        String usernameB = "user_filter_failed_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Failed A", "failed-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Failed B", "failed-b@example.org");

        UserAccount userA = userAccountRepository.findById(userIdA).orElseThrow();
        userA.setFailedLoginAttempts(2);
        userAccountRepository.save(userA);

        UserAccount userB = userAccountRepository.findById(userIdB).orElseThrow();
        userB.setFailedLoginAttempts(7);
        userAccountRepository.save(userB);

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("failedLoginAttempts", "7")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(userIdB))
                .andExpect(jsonPath("$.items[0].failedLoginAttempts").value(7));
    }

    @Test
    void list_users_filters_by_locked_until() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_filter_locked_a_" + UUID.randomUUID();
        String usernameB = "user_filter_locked_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Locked A", "locked-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Locked B", "locked-b@example.org");

        Instant lockedUntilA = Instant.parse("2030-02-01T08:00:00Z");
        Instant lockedUntilB = Instant.parse("2030-03-01T08:00:00Z");

        UserAccount userA = userAccountRepository.findById(userIdA).orElseThrow();
        userA.setLockedUntil(lockedUntilA);
        userAccountRepository.save(userA);

        UserAccount userB = userAccountRepository.findById(userIdB).orElseThrow();
        userB.setLockedUntil(lockedUntilB);
        userAccountRepository.save(userB);

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("lockedUntil", lockedUntilB.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(userIdB))
                .andExpect(jsonPath("$.items[0].lockedUntil").value(lockedUntilB.toString()));
    }

    @Test
    void list_users_filters_by_last_modified_at() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_filter_modified_a_" + UUID.randomUUID();
        String usernameB = "user_filter_modified_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Modified A", "modified-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Modified B", "modified-b@example.org");

        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setDisplayName("User Modified B Updated");
        updateRequest.setEmail("modified-b-updated@example.org");

        mockMvc.perform(
                        put("/api/idm/users/{id}", userIdB)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk());

        UserAccount updatedUserB = userAccountRepository.findById(userIdB).orElseThrow();
        LocalDateTime lastModifiedAtB = updatedUserB.getLastModifiedAt();

        org.junit.jupiter.api.Assertions.assertNotNull(
                lastModifiedAtB,
                "lastModifiedAt must be populated after updating a user"
        );

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("lastModifiedAt", lastModifiedAtB.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id=='" + userIdB + "')]").exists());
    }

    @Test
    void list_users_sorts_by_failed_login_attempts_desc() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_sort_failed_a_" + UUID.randomUUID();
        String usernameB = "user_sort_failed_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Sort Failed A", "sort-failed-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Sort Failed B", "sort-failed-b@example.org");

        UserAccount userA = userAccountRepository.findById(userIdA).orElseThrow();
        userA.setFailedLoginAttempts(1);
        userAccountRepository.save(userA);

        UserAccount userB = userAccountRepository.findById(userIdB).orElseThrow();
        userB.setFailedLoginAttempts(9);
        userAccountRepository.save(userB);

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "50")
                                .param("sortBy", "failedLoginAttempts")
                                .param("sortDir", "desc")
                                .param("username", "user_sort_failed_")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(userIdB))
                .andExpect(jsonPath("$.items[0].failedLoginAttempts").value(9));
    }

    @Test
    void list_users_sorts_by_locked_until_desc() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_sort_locked_a_" + UUID.randomUUID();
        String usernameB = "user_sort_locked_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Sort Locked A", "sort-locked-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Sort Locked B", "sort-locked-b@example.org");

        Instant lockedUntilA = Instant.parse("2030-04-01T08:00:00Z");
        Instant lockedUntilB = Instant.parse("2030-05-01T08:00:00Z");

        UserAccount userA = userAccountRepository.findById(userIdA).orElseThrow();
        userA.setLockedUntil(lockedUntilA);
        userAccountRepository.save(userA);

        UserAccount userB = userAccountRepository.findById(userIdB).orElseThrow();
        userB.setLockedUntil(lockedUntilB);
        userAccountRepository.save(userB);

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "50")
                                .param("sortBy", "lockedUntil")
                                .param("sortDir", "desc")
                                .param("username", "user_sort_locked_")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(userIdB))
                .andExpect(jsonPath("$.items[0].lockedUntil").value(lockedUntilB.toString()));
    }

    @Test
    void list_users_sorts_by_last_modified_at_desc() throws Exception {

        String token = loginAdminAndGetToken();

        String usernameA = "user_sort_modified_a_" + UUID.randomUUID();
        String usernameB = "user_sort_modified_b_" + UUID.randomUUID();

        String userIdA = createUserViaApi(token, usernameA, "User Sort Modified A", "sort-modified-a@example.org");
        String userIdB = createUserViaApi(token, usernameB, "User Sort Modified B", "sort-modified-b@example.org");

        UpdateUserRequestDTO updateRequestA = new UpdateUserRequestDTO();
        updateRequestA.setDisplayName("User Sort Modified A Updated");
        updateRequestA.setEmail("sort-modified-a-updated@example.org");

        mockMvc.perform(
                        put("/api/idm/users/{id}", userIdA)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestA))
                )
                .andExpect(status().isOk());

        UpdateUserRequestDTO updateRequestB = new UpdateUserRequestDTO();
        updateRequestB.setDisplayName("User Sort Modified B Updated");
        updateRequestB.setEmail("sort-modified-b-updated@example.org");

        mockMvc.perform(
                        put("/api/idm/users/{id}", userIdB)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestB))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "50")
                                .param("sortBy", "lastModifiedAt")
                                .param("sortDir", "desc")
                                .param("username", "user_sort_modified_")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(userIdB));
    }

    @Test
    void list_users_rejects_invalid_locked_until_filter() throws Exception {

        String token = loginAdminAndGetToken();

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("lockedUntil", "not-an-instant")
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void list_users_rejects_invalid_last_modified_at_filter() throws Exception {

        String token = loginAdminAndGetToken();

        mockMvc.perform(
                        get("/api/idm/users/list")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "username")
                                .param("sortDir", "asc")
                                .param("lastModifiedAt", "not-a-local-date-time")
                )
                .andExpect(status().is4xxClientError());
    }

    private String createUserViaApi(
            String token,
            String username,
            String displayName,
            String email
    ) throws Exception {

        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername(username);
        request.setDisplayName(displayName);
        request.setEmail(email);
        request.setPassword("password");

        String response = mockMvc.perform(
                        post("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}