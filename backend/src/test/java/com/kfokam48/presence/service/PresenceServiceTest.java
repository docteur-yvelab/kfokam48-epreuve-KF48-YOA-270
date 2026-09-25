package com.kfokam48.presence.service;

import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Promotion;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.exception.CodeExpireException;
import com.kfokam48.presence.exception.DejaPresentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock
    private com.kfokam48.presence.repository.SessionRepository sessionRepository;

    @Mock
    private com.kfokam48.presence.repository.PresenceRepository presenceRepository;

    @Mock
    private com.kfokam48.presence.repository.EtudiantRepository etudiantRepository;

    @InjectMocks
    private PresenceService presenceService;

    private Session session;
    private Etudiant etudiant;
    private String codeValide = "KF48-TEST-001";

    @BeforeEach
    void setUp() {
        Promotion promotion = Promotion.builder().id(1L).nom("Test").annee("2025").build();
        
        session = Session.builder()
                .id(1L)
                .code(codeValide)
                .ouvertureAt(LocalDateTime.now().minusMinutes(5))
                .expirationAt(LocalDateTime.now().plusMinutes(10))
                .cloturee(false)
                .promotion(promotion)
                .build();

        etudiant = Etudiant.builder().id(1L).nom("Test").prenom("User").promotion(promotion).build();
    }

    @Test
    void marquerPresence_codeExpire_lanceCodeExpireException() {
        session.setExpirationAt(LocalDateTime.now().minusMinutes(1));
        
        when(sessionRepository.findByCode(codeValide)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> presenceService.marquerPresence(
                new com.kfokam48.presence.dto.PresenceRequest(codeValide, 1L)))
                .isInstanceOf(CodeExpireException.class)
                .hasFieldOrPropertyWithValue("code", "CODE_EXPIRE");
    }

    @Test
    void marquerPresence_dejaPresent_lanceDejaPresentException() {
        when(sessionRepository.findByCode(codeValide)).thenReturn(Optional.of(session));
        when(presenceRepository.existsBySessionIdAndEtudiantId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> presenceService.marquerPresence(
                new com.kfokam48.presence.dto.PresenceRequest(codeValide, 1L)))
                .isInstanceOf(DejaPresentException.class)
                .hasFieldOrPropertyWithValue("code", "DEJA_PRESENT");
    }

    @Test
    void marquerPresence_casNominal_retournePresenceResponse() {
        when(sessionRepository.findByCode(codeValide)).thenReturn(Optional.of(session));
        when(presenceRepository.existsBySessionIdAndEtudiantId(1L, 1L)).thenReturn(false);
        when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(presenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = presenceService.marquerPresence(
                new com.kfokam48.presence.dto.PresenceRequest(codeValide, 1L));

        assertThat(response).isNotNull();
        assertThat(response.getSource()).isEqualTo("ETUDIANT");
        assertThat(response.getEtudiantId()).isEqualTo(1L);
        assertThat(response.getSessionId()).isEqualTo(1L);
    }

    @Test
    void antiBruteforce_apres5Echecs_bloque2Minutes() {
        when(sessionRepository.findByCode(codeValide)).thenReturn(Optional.of(session));
        when(presenceRepository.existsBySessionIdAndEtudiantId(1L, 1L)).thenReturn(true);

        // 5 tentatives échouées
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> presenceService.marquerPresence(
                    new com.kfokam48.presence.dto.PresenceRequest(codeValide, 1L)))
                    .isInstanceOf(DejaPresentException.class);
        }

        // 6ème tentative : doit être bloquée
        assertThatThrownBy(() -> presenceService.marquerPresence(
                new com.kfokam48.presence.dto.PresenceRequest(codeValide, 1L)))
                .hasMessageContaining("Trop de tentatives");
    }
}