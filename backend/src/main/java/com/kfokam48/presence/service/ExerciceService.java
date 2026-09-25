package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.ExerciceRequest;
import com.kfokam48.presence.dto.ExerciceResponse;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.exception.ExerciceDejaDeposeException;
import com.kfokam48.presence.exception.LienInvalideException;
import com.kfokam48.presence.exception.SessionClotureeException;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final RelectureRepository relectureRepository;
    private final RelectureService relectureService;

    public ExerciceResponse deposerExercice(ExerciceRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable"));

        if (session.isCloturee()) {
            throw new SessionClotureeException();
        }

        validerLien(request.getLien());

        if (exerciceRepository.existsBySessionIdAndEtudiantId(request.getSessionId(), request.getEtudiantId())) {
            throw new ExerciceDejaDeposeException();
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable"));

        Exercice exercice = Exercice.builder()
                .session(session)
                .etudiant(etudiant)
                .lien(request.getLien())
                .statut(Exercice.StatutExercice.DEPOSE)
                .dateDepot(LocalDateTime.now())
                .build();

        exercice = exerciceRepository.save(exercice);
        return toResponse(exercice);
    }

    public ExerciceResponse remplacerLien(Long exerciceId, String nouveauLien) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new IllegalArgumentException("Exercice introuvable"));

        if (exercice.getSession().isCloturee()) {
            throw new SessionClotureeException();
        }

        if (exercice.getStatut() != Exercice.StatutExercice.DEPOSE) {
            throw new IllegalStateException("Le lien ne peut être remplacé que si aucune relecture n'a commencé");
        }

        validerLien(nouveauLien);
        exercice.setLien(nouveauLien);
        exercice.setDateModifLien(LocalDateTime.now());
        exercice = exerciceRepository.save(exercice);
        return toResponse(exercice);
    }

    public void assignerRelecteur(Long exerciceId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new IllegalArgumentException("Exercice introuvable"));

        if (exercice.getStatut() != Exercice.StatutExercice.DEPOSE) {
            return; // Déjà assigné ou relu
        }

        // Trouver les étudiants présents à cette session (sauf l'auteur)
        var presences = exercice.getSession().getPresences(); // Nécessite relation bidirectionnelle ou requête

        // Utiliser une requête custom pour trouver les présents
        // Pour simplifier : on récupère via repository
        // Cette logique sera dans RelectureService
    }

    private void validerLien(String lien) {
        try {
            new URI(lien);
        } catch (URISyntaxException e) {
            throw new LienInvalideException();
        }
    }

    private ExerciceResponse toResponse(Exercice exercice) {
        return ExerciceResponse.builder()
                .id(exercice.getId())
                .statut(exercice.getStatut().name())
                .build();
    }
}