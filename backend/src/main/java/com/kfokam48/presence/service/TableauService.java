package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.TableauEtudiantResponse;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Relecture;
import com.kfokam48.presence.exception.PromotionInconnueException;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.RelectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public List<TableauEtudiantResponse> getTableau(Long promotionId) {
        promotionRepository.findById(promotionId)
                .orElseThrow(PromotionInconnueException::new);

        List<Etudiant> etudiants = etudiantRepository.findByPromotionId(promotionId);

        // Récupérer toutes les sessions de cette promotion
        var sessions = promotionRepository.findById(promotionId).get().getSessions(); // Nécessite relation

        // Pour simplifier : on agrège sur toutes les sessions de la promotion
        // On récupère toutes les présences, exercices, relectures pour ces étudiants
        List<Long> etudiantIds = etudiants.stream().map(Etudiant::getId).toList();

        // Présences par étudiant
        Map<Long, Long> presencesParEtudiant = presenceRepository.findAllByEtudiantIdIn(etudiantIds).stream()
                .collect(Collectors.groupingBy(p -> p.getEtudiant().getId(), Collectors.counting()));

        // Exercices déposés par étudiant
        Map<Long, Long> exercicesParEtudiant = exerciceRepository.findAllByEtudiantIdIn(etudiantIds).stream()
                .collect(Collectors.groupingBy(e -> e.getEtudiant().getId(), Collectors.counting()));

        // Relectures en attente par relecteur
        Map<Long, Long> relecturesEnAttenteParRelecteur = relectureRepository.findAllByRelecteurIdIn(etudiantIds).stream()
                .filter(r -> r.getExercice().getStatut() == Exercice.StatutExercice.EN_ATTENTE_RELECTURE)
                .collect(Collectors.groupingBy(r -> r.getRelecteur().getId(), Collectors.counting()));

        // Moyenne des notes reçues par étudiant (en tant qu'auteur d'exercice)
        Map<Long, Double> moyenneParEtudiant = relectureRepository.findAllByExercice_EtudiantIdIn(etudiantIds).stream()
                .filter(r -> r.getExercice().getStatut() == Exercice.StatutExercice.RELU)
                .collect(Collectors.groupingBy(
                        r -> r.getExercice().getEtudiant().getId(),
                        Collectors.averagingInt(Relecture::getNote)
                ));

        return etudiants.stream()
                .map(e -> TableauEtudiantResponse.builder()
                        .etudiantId(e.getId())
                        .nom(e.getNom() + " " + e.getPrenom())
                        .presences(presencesParEtudiant.getOrDefault(e.getId(), 0L).intValue())
                        .exercicesDeposes(exercicesParEtudiant.getOrDefault(e.getId(), 0L).intValue())
                        .moyenne(moyenneParEtudiant.get(e.getId()))
                        .relecturesEnAttente(relecturesEnAttenteParRelecteur.getOrDefault(e.getId(), 0L).intValue())
                        .build())
                .toList();
    }
}