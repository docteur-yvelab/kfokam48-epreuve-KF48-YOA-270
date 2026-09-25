-- V2__insert_demo_data.sql
-- Données de démonstration pour démarrage rapide

-- Promotion
INSERT INTO promotion (id, nom, annee) VALUES (1, 'KFOKAM48-YOA', '2025-2026');

-- Étudiants (5 étudiants)
INSERT INTO etudiant (id, nom, prenom, email, promotion_id) VALUES
    (1, 'YAO', 'Jean', 'jean.yao@kfokam48.com', 1),
    (2, 'KOUAME', 'Marie', 'marie.kouame@kfokam48.com', 1),
    (3, 'TRAORE', 'Pierre', 'pierre.traore@kfokam48.com', 1),
    (4, 'DIALLO', 'Fatou', 'fatou.diallo@kfokam48.com', 1),
    (5, 'BA', 'Moussa', 'moussa.ba@kfokam48.com', 1);

-- Session ouverte (expiration dans 15 min à partir de maintenant)
-- Le code sera généré par l'application, on met un placeholder
INSERT INTO session (id, titre, code, ouverture_at, expiration_at, cloturee, promotion_id) VALUES
    (1, 'Session Java Spring Boot', 'KF48-DEMO-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '15' MINUTE, FALSE, 1);