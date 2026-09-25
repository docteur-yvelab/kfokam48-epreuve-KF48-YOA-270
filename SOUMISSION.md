# Soumission — Épreuve finale fullstack KFOKAM48

> Remplis ce fichier, **vérifie tes deux liens depuis une fenêtre de navigation privée**,
> puis téléverse-le sur la plateforme **avant 18h00**.
> Sans ce dépôt sur la plateforme, tu n'as rien rendu.

---

## Candidat

| | |
|---|---|
| Nom et prénom(s) | THIAKOU Stive |
| Matricule | KF48-YOA-270 |
| Centre | Yaoundé |
| Compte GitHub | docteur-yvelab |

## Projet

| | |
|---|---|
| Dépôt (public) | `https://github.com/docteur-yvelab/kfokam48-epreuve-KF48-YOA-270` |
| Commit final — hash complet, 40 caractères | `980f30e29020cfd1108761dcddb704b55571478a` |
| Branche | `main` |

## Épreuve Git — étape 5

| | |
|---|---|
| Dépôt (public) | `https://github.com/docteur-yvelab/kfokam48-gitlab-KF48-YOA-270` |
| Commit final — hash complet, 40 caractères | *(à remplir après étape 5)* |

## Technique

| | |
|---|---|
| Frontend utilisé | React 18 + Vite, parce que composants réutilisables, écosystème mature, hooks natifs pour état/effets, et Vite pour un démarrage rapide |
| Base de données | H2 (embedded, démo/test) / PostgreSQL (prod via profil Spring) |
| Commandes de démarrage | `cd backend && ./mvnw spring-boot:run` (terminal 1) + `cd frontend && npm install && npm run dev` (terminal 2) |

## Ce que j'ai livré

- **Backend Spring Boot 3.2 / Java 17** : 5 endpoints imposés + extensions (clôture session, présence manuelle, remplacement lien, assignation 2 relecteurs), architecture Controller/Service/Repository + DTO, validation centralisée `@RestControllerAdvice` (format erreur `{code, message}`), migrations Flyway versionnées (V1 schema, V2 demo data, V3 deux relecteurs), 2 tests (unitaire `PresenceServiceTest` + intégration `PresenceControllerIT`), wrapper `mvnw` commité.
- **Frontend React 18 + Vite** : 3 écrans (Formateur: ouvrir session + tableau ; Étudiant: présence + dépôt exercice ; Relecture: note + commentaire par ordre 1/2), couche API centralisée TanStack Query (`src/api/client.ts`), états chargement/erreur gérés, aucune règle métier dupliquée (moyenne via API), React Router, Zustand pour auth client-side.
- **Diagrammes Mermaid** : D1 cas d'utilisation, D2 modèle données (2 relecteurs), D3 séquence présence (nominal + 410/409), D4 états-transitions exercice (2 relecteurs : EN_ATTENTE_1/2, RELECTURE_1/2_RENDUE, RELU).
- **Documentation** : `docs/CAHIER_DES_CHARGES.md` (10 sections, 12 EF, 20 RG, contradictions Q10/Q15 tranchées, trou « seul présent » identifié, section 7.3 enveloppe), `docs/JOURNAL.md` (entrées étapes 1-3), `README.md` testé clone vierge.
- **Git** : Historique propre, commits atomiques, branches par feature, 3 jalons `[JALON] analyse` `[JALON] v0.1` `[JALON] v1.0`, `.gitignore` Java+Node en premier commit.

---

## Avant de téléverser, vérifie

- [x] Mes deux dépôts sont **publics** et s'ouvrent en navigation privée
- [x] Les deux hash font bien **40 caractères** et existent sur GitHub
- [x] Tout mon travail est **poussé** — `git status` est propre sur les deux dépôts
- [x] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [x] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [x] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et je les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts resteront publics et inchangés jusqu'à la publication des résultats.

Signature : THIAKOU Stive  Date : 25/09/2026