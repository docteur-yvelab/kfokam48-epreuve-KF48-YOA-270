package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.RelectureRequest;
import com.kfokam48.presence.dto.RelectureResponse;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.exception.AucunEtudiantPresentException;
import com.kfokam48.presence.exception.AutoRelectureException;
import com.kfokam48.presence.exception.NoteInvalideException;
import com.kfokam48.presence.exception.RelectureDejaRendueException;
import com.kfokam48.presence.exception.RelectureNonModifiableException;
import com.kfokam48.presence.exception.SessionClotureeException;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class RelectureService {

    private final RelectureRepository relectureRepository;
    private final ExerciceRepository exerciceRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;

    public void assignerRelecteurSiPossible(Long exerciceId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new IllegalArgumentException("Exercice introuvable"));

        if (exercice.getStatut() != Exercice.StatutExercice.DEPOSE) {
            return;
        }

        if (exercice.getSession().isCloturee()) {
            return;
        }

        // Trouver les étudiants présents à cette session (sauf l'auteur)
        List<Long> etudiantsPresents = presenceRepository.findBySessionId(exercice.getSession().getId())
                .stream()
                .map(p -> p.getEtudiant().getId())
                .filter(id -> !id.equals(exercice.getEtudiant().getId()))
                .toList();

        if (etudiantsPresents.isEmpty()) {
            // Trou : aucun autre étudiant présent - l'exercice reste en DEPOSE
            // Le formateur le verra comme "en attente" (relecturesEnAttente = 0 car pas de relecteur assignable)
            return;
        }

        // Assignation aléatoire (Q7, RG5)
        Long relecteurId = etudiantsPresents.get(new Random().nextInt(etudiantsPresents.size()));

        Exercice exerciceMaj = exercice.toBuilder()
                .statut(Exercice.StatutExercice.EN_ATTENTE_RELECTURE)
                .build();
        exerciceRepository.save(exerciceMaj);

        Relecture relecture = Relecture.builder()
                .exercice(exerciceMaj)
                .relecteur(etudiantRepository.getReferenceById(relecteurId))
                .note(0) // Sera mis à jour lors de la soumission
                .commentaire("")
                .dateSoumission(LocalDateTime.now())
                .build();

        relectureRepository.save(relecture);
    }

    public RelectureResponse soumettreRelecture(Long relectureId, RelectureRequest request, Long relecteurId) {
        Relecture relecture = relectureRepository.findById(relectureId)
                .orElseThrow(() -> new IllegalArgumentException("Relecture introuvable"));

        if (!relecture.getRelecteur().getId().equals(relecteurId)) {
            throw new IllegalArgumentException("Non autorisé");
        }

        if (relecture.getExercice().getSession().isCloturee()) {
            throw new SessionClotureeException();
        }

        if (relecture.getDateModification() != null && relecture.getExercice().getSession().isCloturee()) {
            throw new RelectureNonModifiableException();
        }

        validerNote(request.getNote());

        // Vérifier auto-relecture
        if (relecture.getExercice().getEtudiant().getId().equals(relecteurId)) {
            throw new AutoRelectureException();
        }

        boolean premiereSoumission = relecture.getNote() == 0 && relecture.getCommentaire().isEmpty();

        relecture.setNote(request.getNote());
        relecture.setCommentaire(request.getCommentaire());
        if (premiereSoumission) {
            relecture.setDateSoumission(LocalDateTime.now());
        } else {
            relecture.setDateModification(LocalDateTime.now());
        }

        // Mettre à jour le statut de l'exercice
        Exercice exercice = relecture.getExercice();
        exercice.setStatut(Exercice.StatutExercice.RELU);
        exerciceRepository.save(exercice);

        relecture = relectureRepository.save(relecture);
        return toResponse(relecture);
    }

    public RelectureResponse getRelectureByExercice(Long exerciceId) {
        return relectureRepository.findByExerciceId(exerciceId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Aucune relecture pour cet exercice"));
    }

    public List<RelectureResponse> getRelecturesByRelecteur(Long relecteurId) {
        return relectureRepository.findByRelecteurId(relecteurId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validerNote(Integer note) {
        if (note == null || note < 0 || note > 20) {
            throw new NoteInvalideException();
        }
    }

    private RelectureResponse toResponse(Relecture relecture) {
        return RelectureResponse.builder()
                .id(relecture.getId())
                .exerciceId(relecture.getExercice().getId())
                .relecteurId(relecture.getRelecteur().getId())
                .note(relecture.getNote())
                .commentaire(relecture.getCommentaire())
                .dateSoumission(relecture.getDateSoumission())
                .dateModification(relecture.getDateModification())
                .build();
    }
}