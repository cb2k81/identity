package de.cocondo.app.domain.idm.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.domain.idm.auth.dto.LoginRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.domain.idm.scope.dto.CreateApplicationScopeRequestDTO;
import de.cocondo.app.domain.idm.scope.dto.UpdateApplicationScopeRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test for ApplicationScope CRUD.
 *
 * Tests the complete stack:
 * Controller -> Facade -> Handler -> EntityService -> Repository -> DB
 *
 * Baseline:
 * - Login under /api/auth/login
 * - IDM management under /api/idm/**
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "idm.bootstrap.enabled=true",
        "idm.bootstrap.mode=safe",
        "idm.bootstrap.base-path=idm/bootstrap",
        "idm.self.scope.application-key=IDM",
        "idm.self.scope.stage-key=TEST"
})
class ApplicationScopeControllerCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAdminAndGetToken() throws Exception {

        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername("admin");
        req.setPassword("admin");
        req.setApplicationKey("IDM");
        req.setStageKey("TEST");

        MvcResult result = mockMvc.perform(post("/api/auth/login")   // <-- KORREKT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void admin_canCreateReadUpdateDeleteScope_fullStack() throws Exception {

        String token = loginAdminAndGetToken();

        String applicationKey = "APP-" + UUID.randomUUID();
        String stageKey = "STAGE-" + UUID.randomUUID();

        // -------------------------------------------------------
        // CREATE
        // -------------------------------------------------------
        CreateApplicationScopeRequestDTO create = new CreateApplicationScopeRequestDTO();
        create.setApplicationKey(applicationKey);
        create.setStageKey(stageKey);
        create.setDescription("Created by integration test");

        MvcResult createdResult = mockMvc.perform(post("/api/idm/scopes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.applicationKey").value(applicationKey))
                .andExpect(jsonPath("$.stageKey").value(stageKey))
                .andExpect(jsonPath("$.description").value("Created by integration test"))
                .andReturn();

        ApplicationScopeDTO created = objectMapper.readValue(
                createdResult.getResponse().getContentAsString(),
                ApplicationScopeDTO.class
        );

        assertThat(created.getId()).isNotBlank();

        // -------------------------------------------------------
        // READ
        // -------------------------------------------------------
        mockMvc.perform(get("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.applicationKey").value(applicationKey))
                .andExpect(jsonPath("$.stageKey").value(stageKey));

        // -------------------------------------------------------
        // UPDATE (nur description änderbar)
        // -------------------------------------------------------
        UpdateApplicationScopeRequestDTO update = new UpdateApplicationScopeRequestDTO();
        update.setDescription("Updated description");

        mockMvc.perform(put("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.applicationKey").value(applicationKey))
                .andExpect(jsonPath("$.stageKey").value(stageKey));

        // -------------------------------------------------------
        // DELETE
        // -------------------------------------------------------
        mockMvc.perform(delete("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // -------------------------------------------------------
        // VERIFY DELETE
        // -------------------------------------------------------
        mockMvc.perform(get("/api/idm/scopes/{id}", created.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }
}