package com.kfokam48.presence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam48.presence.dto.PresenceRequest;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceConcurrencyIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SessionRepository sessionRepository;

    private String code;
    private Long sessionId;

    @BeforeEach
    void setUp() {
        // Utiliser la session de démo créée par V2 (id=1)
        Session session = sessionRepository.findById(1L).orElseThrow();
        this.code = session.getCode();
        this.sessionId = session.getId();
    }

    @Test
    void deuxEtudiantsMarquentPresenceSimultanement_lesDeuxEnregistrees() throws Exception {
        // When: deux requêtes simultanées pour deux étudiants différents
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 1; i <= 2; i++) {
            final int etudiantId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // départ simultané
                    PresenceRequest req = new PresenceRequest(code, (long) etudiantId);
                    mockMvc.perform(post("/api/presences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                            .andExpect(status().isCreated());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // GO !
        endLatch.await(); // attend fin des 2 threads
        executor.shutdown();

        // Then: les deux doivent réussir
        assert successCount.get() == 2 : "Les deux présences doivent être créées, succès=" + successCount + ", erreurs=" + errorCount;
    }
}