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
    void deuxAppelsSimultanesPourMemeEtudiant_unSeulEnregistre() throws Exception {
        // Given: un code valide et un étudiant
        // When: deux requêtes simultanées POUR LE MEME ETUDIANT
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // départ simultané
                    PresenceRequest req = new PresenceRequest(code, 1L); // MÊME étudiantId = 1
                    var result = mockMvc.perform(post("/api/presences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
                    int status = result.andReturn().getResponse().getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
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

        // Then: exactement UNE présence créée (201) + UN conflit (409)
        // car la contrainte d'unicité (sessionId, etudiantId) doit être respectée
        assert successCount.get() == 1 : "Une seule présence doit être créée pour le même étudiant, succès=" + successCount + ", conflit=" + conflictCount + ", erreurs=" + errorCount;
        assert conflictCount.get() == 1 : "Un conflit 409 attendu, conflit=" + conflictCount;
    }

    @Test
    void deuxEtudiantsDifferentsSimultanement_lesDeuxEnregistrees() throws Exception {
        // Given: un code valide
        // When: deux requêtes simultanées POUR DEUX ETUDIANTS DIFFERENTS
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
                    var result = mockMvc.perform(post("/api/presences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
                    int status = result.andReturn().getResponse().getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
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

        // Then: les deux doivent réussir (étudiants différents = pas de conflit unicité)
        assert successCount.get() == 2 : "Les deux présences doivent être créées pour étudiants différents, succès=" + successCount + ", erreurs=" + errorCount;
    }
}
