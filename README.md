# KFOKAM48 - Épreuve Finale Fullstack

**Matricule :** KF48-YOA-270  
**Frontend choisi :** React, parce que composants réutilisables, écosystème mature et hooks natifs pour la gestion d'état

---

## Structure du dépôt

```
/docs           CAHIER_DES_CHARGES.md · JOURNAL.md · diagrammes/
/api            contrat.yaml (OpenAPI 3.0)
/backend        Spring Boot 3 (Java 17, Maven, mvnw)
/frontend       React 18 + Vite
```

---

## Démarrage rapide (3 commandes)

```bash
# 1. Backend
cd backend && ./mvnw spring-boot:run

# 2. Frontend (nouveau terminal)
cd frontend && npm install && npm run dev

# 3. Accès
# Frontend : http://localhost:5173
# API      : http://localhost:8080
# Swagger  : http://localhost:8080/swagger-ui.html
```

> **Données de démo** : Une promotion (id=1), 5 étudiants, 1 session ouverte au démarrage via `data.sql` / `CommandLineRunner`.

---

## Contraintes techniques respectées

| Backend (B1-B6) | Frontend (F1-F3) |
|-----------------|------------------|
| Java 17, Maven, mvnw commité | Framework déclaré + justifié |
| Contrat API `api/contrat.yaml` respecté | 3 écrans : formateur / étudiant / relecteur |
| Couches Controller/Service/Repository + DTO | Couche API dédiée (pas de fetch dispersé) |
| Validation + @RestControllerAdvice centralisé | États chargement/erreur gérés |
| Flyway migrations versionnées, ddl-auto=none | Aucune règle métier dupliquée |
| 2 tests (unitaire + intégration) | Build passe (`npm run build`) |

---

## Jalons Git (ordre imposé)

1. `[JALON] analyse` — **poussé** (commit `b3133d9`)
2. `[JALON] v0.1` — à venir (étape 2)
3. `[JALON] v1.0` — à venir (étape 4)

---

## Commandes utiles

```bash
# Backend tests
cd backend && ./mvnw test

# Frontend build
cd frontend && npm run build

# Vérifier contrat API
# (ouvrir api/contrat.yaml dans Swagger Editor)
```