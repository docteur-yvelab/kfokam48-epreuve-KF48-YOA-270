package com.kfokam48.presence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam48.presence.dto.PresenceRequest;
import com.kfokam48.presence.dto.SessionRequest;
import com.kfokam48.presence.dto.SessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ouvrirSession_puisMarquerPresence_casNominal() throws Exception {
        // 1. Ouvrir une session
        SessionRequest sessionRequest = new SessionRequest("Test Session", 1L);
        String sessionResponseJson = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.expirationAt").exists())
                .andReturn().getResponse().getContentAsString();

        SessionResponse sessionResponse = objectMapper.readValue(sessionResponseJson, SessionResponse.class);
        String code = sessionResponse.getCode();

        // 2. Marquer présence avec le code
        PresenceRequest presenceRequest = new PresenceRequest(code, 1L);
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presenceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("ETUDIANT"))
                .andExpect(jsonPath("$.sessionId").value(sessionResponse.getId()));
    }

    @Test
    void marquerPresence_codeExpire_retourne410() throws Exception {
        // Créer une session expirée via SQL direct ou attendre
        // Pour ce test, on suppose qu'une session existe avec un code expiré
        // Ce test nécessite une session avec expirationAt < NOW
        
        PresenceRequest request = new PresenceRequest("CODE-EXPIRE", 1L);
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void marquerPresence_dejaPresent_retourne409() throws Exception {
        // D'abord marquer la présence
        SessionRequest sessionRequest = new SessionRequest("Test Session 2", 1L);
        String sessionResponseJson = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        SessionResponse sessionResponse = objectMapper.readValue(sessionResponseJson, SessionResponse.class);
        String code = sessionResponse.getCode();

        PresenceRequest presenceRequest = new PresenceRequest(code, 1L);
        
        // Première tentative - succès
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presenceRequest)))
                .andExpect(status().isCreated());

        // Deuxième tentative - conflit
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presenceRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    void marquerPresence_codeInconnu_retourne400() throws Exception {
        PresenceRequest request = new PresenceRequest("CODE-INCONNU", 1L);
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
    }
}