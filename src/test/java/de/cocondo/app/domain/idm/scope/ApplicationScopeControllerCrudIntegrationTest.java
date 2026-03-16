package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.AbstractIdmIntegrationTest;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.CreateApplicationScopeRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.UpdateApplicationScopeRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationScopeControllerCrudIntegrationTest extends AbstractIdmIntegrationTest {

    @Test
    void admin_canCreateReadUpdateDeleteScope_fullStack() throws Exception {

        String token = loginAdminAndGetToken();

        String applicationKeyRandom = "APP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String stageKeyRandom = "STAGE_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        CreateApplicationScopeRequestDTO create = new CreateApplicationScopeRequestDTO();
        create.setApplicationKey(applicationKeyRandom);
        create.setStageKey(stageKeyRandom);
        create.setDescription("Created by integration test");

        MvcResult createdResult = mockMvc.perform(post("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationKey").value(applicationKeyRandom))
                .andExpect(jsonPath("$.stageKey").value(stageKeyRandom))
                .andReturn();

        ApplicationScopeDTO created = objectMapper.readValue(
                createdResult.getResponse().getContentAsString(),
                ApplicationScopeDTO.class
        );

        assertThat(created.getId()).isNotBlank();

        mockMvc.perform(get("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.applicationKey").value(applicationKeyRandom))
                .andExpect(jsonPath("$.stageKey").value(stageKeyRandom));

        UpdateApplicationScopeRequestDTO update = new UpdateApplicationScopeRequestDTO();
        update.setDescription("Updated description");

        mockMvc.perform(put("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.applicationKey").value(applicationKeyRandom))
                .andExpect(jsonPath("$.stageKey").value(stageKeyRandom));

        mockMvc.perform(delete("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }
}