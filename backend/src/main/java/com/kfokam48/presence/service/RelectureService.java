package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.RelectureRequest;
import com.kfokam48.presence.dto.RelectureResponse;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.exception.AucunEtudiantPresentException;
import com.kfokam48.presence.exception.EtudiantInconnuException;
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

    /**
     * Assigne DEUX relecteurs distincts parmi les présents à la session (sauf l'auteur).
     * Appelé automatiquement après dépôt d'exercice.
     */
    public void assignerDeuxRelecteurs(Long exerciceId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new IllegalArgumentException("Exercice introuvable"));

        if (exercice.getStatut() != Exercice.StatutExercice.DEPOSE) {
            return; // Déjà assigné
        }

        if (exercice.getSession().isCloturee()) {
            return;
        }

        // Trouver les étudiants présents à cette session (sauf l'auteur)
        List<Long> etudiantsPresents = presenceRepository.findBySessionId(exercice.getSession().getId())
                .stream()
                .map(p -> p.getEtudiant().getId())
                .filter(id -> !id.equals(exercice.getEtudiant().getId()))
                .distinct()
                .toList();

        if (etudiantsPresents.size() < 2) {
            // Trou : pas assez de pairs pour 2 relecteurs distincts
            // L'exercice reste en DEPOSE, le formateur le verra comme "en attente" (0 relecteur assignable)
            return;
        }

        // Assignation aléatoire de 2 relecteurs DISTINCTS (Q7, RG5, RG4)
        Random random = new Random();
        List<Long> selectionnes = random.ints(0, etudiantsPresents.size())
                .distinct()
                .limit(2)
                .mapToObj(etudiantsPresents::get)
                .toList();

        Long relecteur1Id = selectionnes.get(0);
        Long relecteur2Id = selectionnes.get(1);

        // Mettre à jour le statut de l'exercice
        Exercice exerciceMaj = exercice.toBuilder()
                .statut(Exercice.StatutExercice.EN_ATTENTE_RELECTURE)
                .build();
        exerciceRepository.save(exerciceMaj);

        // Créer relecture 1
        Relecture relecture1 = Relecture.builder()
                .exercice(exerciceMaj)
                .relecteur(etudiantRepository.getReferenceById(relecteur1Id))
                .note(0)
                .commentaire("")
                .dateSoumission(LocalDateTime.now())
                .ordreRelecteur((short) 1)
                .noteProvisoire(true)
                .build();

        // Créer relecture 2
        Relecture relecture2 = Relecture.builder()
                .exercice(exerciceMaj)
                .relecteur(etudiantRepository.getReferenceById(relecteur2Id))
                .note(0)
                .commentaire("")
                .dateSoumission(LocalDateTime.now())
                .ordreRelecteur((short) 2)
                .noteProvisoire(true)
                .build();

        relectureRepository.saveAll(List.of(relecture1, relecture2));
    }

    /**
     * Soumet une relecture (ordre 1 ou 2) pour un exercice.
     */
    public RelectureResponse soumettreRelecture(Long exerciceId, Short ordreRelecteur, RelectureRequest request, Long relecteurId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new IllegalArgumentException("Exercice introuvable"));

        if (exercice.getSession().isCloturee()) {
            throw new SessionClotureeException();
        }

        Relecture relecture = relectureRepository.findByExerciceIdAndOrdreRelecteur(exerciceId, ordreRelecteur)
                .orElseThrow(() -> new IllegalArgumentException("Relecture introuvable pour cet ordre"));

        // Vérifier que c'est le bon relecteur
        if (!relecture.getRelecteur().getId().equals(relecteurId)) {
            throw new IllegalArgumentException("Non autorisé : ce n'est pas votre relecture");
        }

        // Vérifier auto-relecture
        if (relecture.getExercice().getEtudiant().getId().equals(relecteurId)) {
            throw new AutoRelectureException();
        }

        validerNote(request.getNote());

        boolean premiereSoumission = relecture.getNote() == 0 && relecture.getCommentaire().isEmpty();

        relecture.setNote(request.getNote());
        relecture.setCommentaire(request.getCommentaire());
        if (premiereSoumission) {
            relecture.setDateSoumission(LocalDateTime.now());
        } else {
            relecture.setDateModification(LocalDateTime.now());
        }

        // Recalculer la moyenne et mettre à jour les flags noteProvisoire
        recalculerMoyenneEtFlags(exerciceId);

        relecture = relectureRepository.save(relecture);
        return toResponse(relecture);
    }

    /**
     * Recalcule la moyenne des 2 notes et met à jour noteProvisoire sur les deux relectures.
     */
    private void recalculerMoyenneEtFlags(Long exerciceId) {
        List<Relecture> relectures = relectureRepository.findByExerciceIdOrderByOrdreRelecteurAsc(exerciceId);
        
        long countRendues = relectures.stream()
                .filter(r -> r.getNote() != 0 || !r.getCommentaire().isEmpty())
                .count();

        boolean uneSeuleRendue = countRendues == 1;
        boolean lesDeuxRendues = countRendues == 2;

        for (Relecture r : relectures) {
            r.setNoteProvisoire(uneSeuleRendue);
        }

        // Mettre à jour le statut de l'exercice
        Exercice exercice = exerciceRepository.findById(exerciceId).orElseThrow();
        if (lesDeuxRendues) {
            exercice.setStatut(Exercice.StatutExercice.RELU);
        } else if (uneSeuleRendue) {
            exercice.setStatut(Exercice.StatutExercice.EN_ATTENTE_RELECTURE);
        }
        exerciceRepository.save(exercice);
        
        relectureRepository.saveAll(relectures);
    }

    public List<RelectureResponse> getRelecturesByExercice(Long exerciceId) {
        return relectureRepository.findByExerciceIdOrderByOrdreRelecteurAsc(exerciceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RelectureResponse> getRelecturesByRelecteur(Long relecteurId) {
        verifierEtudiant(relecteurId);
        return relectureRepository.findByRelecteurId(relecteurId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void verifierEtudiant(Long etudiantId) {
        if (etudiantId == null || !etudiantRepository.existsById(etudiantId)) {
            throw new EtudiantInconnuException();
        }
    }

    public List<RelectureResponse> getRelecturesEnAttentePourRelecteur(Long relecteurId) {
        verifierEtudiant(relecteurId);
        return relectureRepository.findByRelecteurId(relecteurId)
                .stream()
                .filter(r -> (r.getNote() == 0 && r.getCommentaire().isEmpty()) && !r.getExercice().getSession().isCloturee())
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
                .ordreRelecteur(relecture.getOrdreRelecteur())
                .relecteurId(relecture.getRelecteur().getId())
                .note(relecture.getNote())
                .commentaire(relecture.getCommentaire())
                .dateSoumission(relecture.getDateSoumission())
                .dateModification(relecture.getDateModification())
                // FIX : le flag était maintenu en base mais jamais exposé par l'API
                .noteProvisoire(Boolean.TRUE.equals(relecture.getNoteProvisoire()))
                .exerciceLien(relecture.getExercice().getLien())
                // Tant que la relecture n'est pas rendue, dateSoumission == date d'assignation
                .dateAssignation(relecture.getDateSoumission())
                .build();
    }
}