package com.kfokam48.presence.config;

import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.entity.Promotion;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (promotionRepository.count() > 0) {
            return; // Données déjà présentes
        }

        // Promotion
        Promotion promotion = Promotion.builder()
                .nom("KFOKAM48-YOA")
                .annee("2025-2026")
                .build();
        promotion = promotionRepository.save(promotion);

        // Étudiants
        String[][] etudiantsData = {
            {"YAO", "Jean", "jean.yao@kfokam48.com"},
            {"KOUAME", "Marie", "marie.kouame@kfokam48.com"},
            {"TRAORE", "Pierre", "pierre.traore@kfokam48.com"},
            {"DIALLO", "Fatou", "fatou.diallo@kfokam48.com"},
            {"BA", "Moussa", "moussa.ba@kfokam48.com"}
        };

        for (String[] data : etudiantsData) {
            Etudiant etudiant = Etudiant.builder()
                    .nom(data[0])
                    .prenom(data[1])
                    .email(data[2])
                    .promotion(promotion)
                    .build();
            etudiantRepository.save(etudiant);
        }

        // Session ouverte avec code unique
        String code = genererCodeUnique();
        LocalDateTime now = LocalDateTime.now();

        Session session = Session.builder()
                .titre("Session Java Spring Boot")
                .code(code)
                .ouvertureAt(now)
                .expirationAt(now.plusMinutes(15))
                .cloturee(false)
                .promotion(promotion)
                .build();

        sessionRepository.save(session);
    }

    private String genererCodeUnique() {
        String code;
        Random random = new Random();
        do {
            code = "KF48-" + String.format("%06d", random.nextInt(1_000_000));
        } while (sessionRepository.findByCode(code).isPresent());
        return code;
    }
}