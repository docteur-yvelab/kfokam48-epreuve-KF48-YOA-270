package com.kfokam48.presence.config;

import com.kfokam48.presence.dto.ExerciceRequest;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Promotion;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.ExerciceRepository;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.service.ExerciceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Jeu de données de démonstration garanti à CHAQUE démarrage (idempotent) :
 *  1. promotion + 5 étudiants (si absents) ;
 *  2. session de démo OUVERTE et non expirée (code KF48-DEMO-001) → test du frontend ;
 *  3. session expirée de code CODE-EXPIRE → support du test d'intégration 410 ;
 *  4. présences des étudiants 3, 4 et 5 (les 2 premiers restent libres pour les
 *     tests de concurrence qui attendent 201 puis 409) ;
 *  5. un exercice déposé par le 1er étudiant avec ses 2 relecteurs assignés
 *     → l'écran Relecture a du contenu immédiatement.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final String CODE_DEMO = "KF48-DEMO-001";
    private static final String CODE_EXPIRE = "CODE-EXPIRE";
    private static final int DUREE_DEMO_MINUTES = 180;

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final SessionRepository sessionRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final ExerciceService exerciceService;

    @Override
    @Transactional
    public void run(String... args) {
        Promotion promotion = assurerPromotionEtEtudiants();
        Session demo = assurerSessionDemo(promotion);
        assurerSessionExpiree(promotion);

        if (demo == null) {
            return;
        }
        assurerPresencesDemo(demo);
        assurerExerciceDemo(demo);

        log.info("==============================================================");
        log.info(" DONNEES DE DEMO PRETES");
        log.info("   Session : {} | CODE DE PRESENCE = {}", demo.getTitre(), demo.getCode());
        log.info("   Expire le : {}", demo.getExpirationAt());
        log.info("   Frontend : http://localhost:5173   API : http://localhost:8080");
        log.info("==============================================================");
    }

    private Promotion assurerPromotionEtEtudiants() {
        Promotion promotion = promotionRepository.findAll().stream().findFirst()
                .orElseGet(() -> promotionRepository.save(Promotion.builder()
                        .nom("KFOKAM48-YOA")
                        .annee("2025-2026")
                        .build()));

        if (etudiantRepository.findByPromotionId(promotion.getId()).isEmpty()) {
            String[][] etudiantsData = {
                {"YAO", "Jean", "jean.yao@kfokam48.com"},
                {"KOUAME", "Marie", "marie.kouame@kfokam48.com"},
                {"TRAORE", "Pierre", "pierre.traore@kfokam48.com"},
                {"DIALLO", "Fatou", "fatou.diallo@kfokam48.com"},
                {"BA", "Moussa", "moussa.ba@kfokam48.com"}
            };
            for (String[] data : etudiantsData) {
                etudiantRepository.save(Etudiant.builder()
                        .nom(data[0])
                        .prenom(data[1])
                        .email(data[2])
                        .promotion(promotion)
                        .build());
            }
        }
        return promotion;
    }

    /** La session de démo doit rester utilisable : le code de V2 expire 15 min après la migration. */
    private Session assurerSessionDemo(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        Session session = sessionRepository.findByCode(CODE_DEMO).orElse(null);

        if (session == null) {
            return sessionRepository.save(Session.builder()
                    .titre("Session Java Spring Boot")
                    .code(CODE_DEMO)
                    .ouvertureAt(now)
                    .expirationAt(now.plusMinutes(DUREE_DEMO_MINUTES))
                    .cloturee(false)
                    .promotion(promotion)
                    .build());
        }

        boolean modifiee = false;
        if (session.isExpiree()) {
            session.setOuvertureAt(now);
            session.setExpirationAt(now.plusMinutes(DUREE_DEMO_MINUTES));
            modifiee = true;
        }
        if (session.isCloturee()) {
            session.setCloturee(false);
            session.setClotureAt(null);
            modifiee = true;
        }
        return modifiee ? sessionRepository.save(session) : session;
    }

    /** Session volontairement expirée : support du test « CODE_EXPIRE → 410 ». */
    private void assurerSessionExpiree(Promotion promotion) {
        if (sessionRepository.findByCode(CODE_EXPIRE).isPresent()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.save(Session.builder()
                .titre("Session expirée (test)")
                .code(CODE_EXPIRE)
                .ouvertureAt(now.minusHours(2))
                .expirationAt(now.minusHours(1))
                .cloturee(false)
                .promotion(promotion)
                .build());
    }

    private void assurerPresencesDemo(Session demo) {
        List<Etudiant> etudiants = etudiantRepository.findByPromotionId(demo.getPromotion().getId());
        for (int i = 0; i < etudiants.size(); i++) {
            if (i < 2) {
                continue; // les 2 premiers restent libres pour les tests d'intégration
            }
            Etudiant etudiant = etudiants.get(i);
            if (presenceRepository.existsBySessionIdAndEtudiantId(demo.getId(), etudiant.getId())) {
                continue;
            }
            presenceRepository.save(Presence.builder()
                    .session(demo)
                    .etudiant(etudiant)
                    .dateHeure(LocalDateTime.now())
                    .source(Presence.SourcePresence.ETUDIANT)
                    .build());
        }
    }

    private void assurerExerciceDemo(Session demo) {
        List<Etudiant> etudiants = etudiantRepository.findByPromotionId(demo.getPromotion().getId());
        if (etudiants.isEmpty()) {
            return;
        }
        Long auteurId = etudiants.get(0).getId();
        if (exerciceRepository.existsBySessionIdAndEtudiantId(demo.getId(), auteurId)) {
            return;
        }
        try {
            exerciceService.deposerExercice(ExerciceRequest.builder()
                    .sessionId(demo.getId())
                    .etudiantId(auteurId)
                    .lien("https://github.com/kfokam48/demo-exercice")
                    .build());
            log.info(" Exercice de démo déposé par l'étudiant {} (2 relecteurs assignés)", auteurId);
        } catch (RuntimeException ex) {
            log.warn(" Exercice de démo non créé : {}", ex.getMessage());
        }
    }
}