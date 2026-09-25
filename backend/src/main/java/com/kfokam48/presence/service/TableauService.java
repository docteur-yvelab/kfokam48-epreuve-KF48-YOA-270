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

import java.util.ArrayList;
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
        List<Long> etudiantIds = etudiants.stream().map(Etudiant::getId).toList();

        // Présences par étudiant
        Map<Long, Long> presencesParEtudiant = presenceRepository.findAllByEtudiantIdIn(etudiantIds).stream()
                .collect(Collectors.groupingBy(p -> p.getEtudiant().getId(), Collectors.counting()));

        // Exercices déposés par étudiant
        Map<Long, Long> exercicesParEtudiant = exerciceRepository.findAllByEtudiantIdIn(etudiantIds).stream()
                .collect(Collectors.groupingBy(e -> e.getEtudiant().getId(), Collectors.counting()));

        // Relectures en attente par relecteur (relectures non rendues)
        Map<Long, Long> relecturesEnAttenteParRelecteur = relectureRepository.findAllByRelecteurIdIn(etudiantIds).stream()
                .filter(r -> (r.getNote() == 0 && r.getCommentaire().isEmpty()) && !r.getExercice().getSession().isCloturee())
                .collect(Collectors.groupingBy(r -> r.getRelecteur().getId(), Collectors.counting()));

        // Pour chaque étudiant (auteur d'exercices), calculer la moyenne de ses exercices
        // et déterminer si la moyenne est provisoire (une seule relecture sur au moins un exercice)
        List<Exercice> tousExercices = exerciceRepository.findAllByEtudiantIdIn(etudiantIds);

        // Calculer la moyenne par étudiant (moyenne des moyennes d'exercices)
        Map<Long, Double> moyenneParEtudiant = new java.util.HashMap<>();
        Map<Long, Boolean> moyenneProvisoireParEtudiant = new java.util.HashMap<>();

        for (Exercice ex : tousExercices) {
            Long etudiantId = ex.getEtudiant().getId();
            List<Relecture> relectures = relectureRepository.findByExerciceIdOrderByOrdreRelecteurAsc(ex.getId());

            List<Integer> notes = relectures.stream()
                    .filter(r -> r.getNote() != 0 || !r.getCommentaire().isEmpty())
                    .map(Relecture::getNote)
                    .toList();

            if (!notes.isEmpty()) {
                double moyenneEx = notes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                
                // Mettre à jour la moyenne par étudiant (moyenne des moyennes d'exercices)
                List<Double> moyennesExistantes = new ArrayList<>();
                // On récupère les exercices déjà traités pour cet étudiant
                for (Exercice ex2 : tousExercices) {
                    if (ex2.getEtudiant().getId().equals(etudiantId)) {
                        List<Relecture> r2 = relectureRepository.findByExerciceIdOrderByOrdreRelecteurAsc(ex2.getId());
                        List<Integer> n2 = r2.stream()
                                .filter(r -> r.getNote() != 0 || !r.getCommentaire().isEmpty())
                                .map(Relecture::getNote)
                                .toList();
                        if (!n2.isEmpty()) {
                            double m = n2.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                            moyennesExistantes.add(m);
                        }
                    }
                }
                double moyenneEtudiant = moyennesExistantes.stream().mapToDouble(d -> d).average().orElse(0.0);
                moyenneParEtudiant.put(etudiantId, moyenneEtudiant);

                // Déterminer si provisoire
                boolean provisoire = false;
                for (Exercice ex2 : tousExercices) {
                    if (ex2.getEtudiant().getId().equals(etudiantId)) {
                        List<Relecture> r2 = relectureRepository.findByExerciceIdOrderByOrdreRelecteurAsc(ex2.getId());
                        long countRendues = r2.stream()
                                .filter(r -> r.getNote() != 0 || !r.getCommentaire().isEmpty())
                                .count();
                        if (countRendues == 1) {
                            provisoire = true;
                            break;
                        }
                    }
                }
                moyenneProvisoireParEtudiant.put(etudiantId, provisoire);
            }
        }

        // Relectures en attente par relecteur (pour affichage dans tableau)
        // Already computed above

        return etudiants.stream()
                .map(e -> TableauEtudiantResponse.builder()
                        .etudiantId(e.getId())
                        .nom(e.getNom() + " " + e.getPrenom())
                        .presences(presencesParEtudiant.getOrDefault(e.getId(), 0L).intValue())
                        .exercicesDeposes(exercicesParEtudiant.getOrDefault(e.getId(), 0L).intValue())
                        .moyenne(moyenneParEtudiant.get(e.getId()))
                        .moyenneProvisoire(moyenneProvisoireParEtudiant.getOrDefault(e.getId(), false))
                        .relecturesEnAttente(relecturesEnAttenteParRelecteur.getOrDefault(e.getId(), 0L).intValue())
                        .build())
                .toList();
    }
}