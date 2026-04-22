// Datei: src/test/java/de/cocondo/app/domain/idm/user/UserAccountControllerIntegrationTest.java
package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAccountControllerIntegrationTest extends AbstractIdmIntegrationTest {

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
    void list_users() throws Exception {

        String token = loginAdminAndGetToken();

        mockMvc.perform(
                        get("/api/idm/users")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
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
}