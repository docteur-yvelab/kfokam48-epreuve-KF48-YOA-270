package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.ExerciceDetailResponse;
import com.kfokam48.presence.dto.ExerciceLienResponse;
import com.kfokam48.presence.dto.ExerciceRequest;
import com.kfokam48.presence.dto.ExerciceResponse;
import com.kfokam48.presence.dto.RelectureResponse;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.exception.EtudiantInconnuException;
import com.kfokam48.presence.exception.ExerciceDejaDeposeException;
import com.kfokam48.presence.exception.ExerciceInconnuException;
import com.kfokam48.presence.exception.LienInvalideException;
import com.kfokam48.presence.exception.RelectureDejaCommenceeException;
import com.kfokam48.presence.exception.SessionClotureeException;
import com.kfokam48.presence.exception.SessionInconnueException;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final RelectureService relectureService;

    public ExerciceResponse deposerExercice(ExerciceRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(SessionInconnueException::new);

        if (session.isCloturee()) {
            throw new SessionClotureeException();
        }

        validerLien(request.getLien());

        if (exerciceRepository.existsBySessionIdAndEtudiantId(request.getSessionId(), request.getEtudiantId())) {
            throw new ExerciceDejaDeposeException();
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(EtudiantInconnuException::new);

        Exercice exercice = Exercice.builder()
                .session(session)
                .etudiant(etudiant)
                .lien(request.getLien())
                .statut(Exercice.StatutExercice.DEPOSE)
                .dateDepot(LocalDateTime.now())
                .build();

        exercice = exerciceRepository.save(exercice);

        // Assigner automatiquement les 2 relecteurs après dépôt
        relectureService.assignerDeuxRelecteurs(exercice.getId());

        return toResponse(exercice);
    }

    public ExerciceLienResponse remplacerLien(Long exerciceId, String nouveauLien) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceInconnuException::new);

        if (exercice.getSession().isCloturee()) {
            throw new SessionClotureeException();
        }

        if (exercice.getStatut() != Exercice.StatutExercice.DEPOSE) {
            throw new RelectureDejaCommenceeException();
        }

        validerLien(nouveauLien);
        exercice.setLien(nouveauLien);
        exercice.setDateModifLien(LocalDateTime.now());
        exercice = exerciceRepository.save(exercice);
        return ExerciceLienResponse.builder()
                .id(exercice.getId())
                .lien(exercice.getLien())
                .dateModifLien(exercice.getDateModifLien())
                .build();
    }

    /** GET /api/exercices?sessionId=&etudiantId= (contrat) — 0 ou 1 exercice (unicité session+étudiant). */
    public List<ExerciceDetailResponse> listerParEtudiant(Long sessionId, Long etudiantId) {
        sessionRepository.findById(sessionId).orElseThrow(SessionInconnueException::new);
        etudiantRepository.findById(etudiantId).orElseThrow(EtudiantInconnuException::new);
        return exerciceRepository.findBySessionIdAndEtudiantId(sessionId, etudiantId)
                .map(exercice -> List.of(toDetailResponse(exercice)))
                .orElseGet(() -> List.of());
    }

    /** GET /api/exercices/{id} (contrat) — détail utilisé par l'écran de relecture. */
    public ExerciceDetailResponse getExerciceDetail(Long exerciceId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(ExerciceInconnuException::new);
        return toDetailResponse(exercice);
    }

    private ExerciceDetailResponse toDetailResponse(Exercice exercice) {
        List<RelectureResponse> relectures = relectureService.getRelecturesByExercice(exercice.getId());
        RelectureResponse rendue = relectures.stream()
                .filter(r -> estRendue(r))
                .findFirst()
                .orElse(null);
        return ExerciceDetailResponse.builder()
                .id(exercice.getId())
                .sessionId(exercice.getSession().getId())
                .etudiantId(exercice.getEtudiant().getId())
                .lien(exercice.getLien())
                .statut(exercice.getStatut().name())
                .dateDepot(exercice.getDateDepot())
                .note(rendue != null ? rendue.getNote() : null)
                .commentaire(rendue != null ? rendue.getCommentaire() : null)
                // "relecture" : objet singulier exigé par le contrat ; "relectures" : les 2, pour le frontend
                .relecture(relectures.isEmpty() ? null : relectures.get(0))
                .relectures(relectures)
                .build();
    }

    private boolean estRendue(RelectureResponse r) {
        boolean noteRendue = r.getNote() != null && r.getNote() != 0;
        boolean commentaireRendu = r.getCommentaire() != null && !r.getCommentaire().isEmpty();
        return noteRendue || commentaireRendu;
    }

    private void validerLien(String lien) {
        try {
            new URI(lien);
        } catch (URISyntaxException e) {
            throw new LienInvalideException();
        }
    }

    public List<RelectureResponse> getRelecturesByExercice(Long exerciceId) {
        return relectureService.getRelecturesByExercice(exerciceId);
    }

    private ExerciceResponse toResponse(Exercice exercice) {
        return ExerciceResponse.builder()
                .id(exercice.getId())
                .statut(exercice.getStatut().name())
                .build();
    }

    private ExerciceResponse toResponseAvecRelectures(Exercice exercice) {
        return ExerciceResponse.builder()
                .id(exercice.getId())
                .statut(exercice.getStatut().name())
                .build();
    }
}