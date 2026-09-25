# Cahier des charges — PresenceKFOKAM

**Auteur :** THIAKOU Stive · KF48-YOA-270  
**Version :** 1 · **Date :** 25/09/2026  
**Frontend choisi :** React, parce que composants réutilisables, écosystème mature, hooks natifs pour état et effets, et Vite pour un démarrage rapide.

> Ce document est la référence contractuelle du projet. Toute évolution est tracée dans le journal des révisions (section finale).

---

## 1. Contexte et objectif

L'application **PresenceKFOKAM** répond au besoin de la direction de formation KFOKAM48 de digitaliser la gestion des présences et des relectures d'exercices lors des sessions de cours.

**Problème actuel :** Le suivi papier des présences est source d'erreurs, de fraudes (codes partagés), et ne permet pas de corréler présence / dépôt d'exercices / relectures pairs. Le formateur n'a pas de vue synthétique temps réel.

**Objectif :** Fournir une application web (mobile-first pour les étudiants) permettant :
- Au formateur : ouvrir une session → obtenir un code de présence à durée limitée (15 min), voir un tableau récapitulatif par étudiant (présences, exercices déposés, moyenne notes, relectures en attente), ajouter une présence manuelle tracée, clôturer la session.
- À l'étudiant : marquer sa présence via le code, déposer le lien de son exercice (jusqu'à clôture), être assigné aléatoirement comme relecteur d'un pair présent, consulter sa note et le commentaire (anonyme).
- Au relecteur (étudiant dans un état) : noter sur 20 (entier) + commentaire l'exercice assigné, modifier sa note tant que la session n'est pas clôturée.

**Cible :** Formateurs et étudiants KFOKAM48. Utilisation en salle de cours (WiFi) et hors connexion (dépôt différé d'exercices).

---

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|--------|---------------------|----------------------------|
| **Formateur** | Ouvrir une session (titre + promotion) → reçoit code unique<br>Voir le tableau récapitulatif (promotion) : présences, exercices, moyenne, relectures en attente<br>Ajouter une présence manuelle (source = FORMATEUR)<br>Clôturer la session (verrouille notes, assignations, dépôts) | Marquer une présence pour un étudiant sans tracer "FORMATEUR"<br>Modifier une note de relecture<br>Voir le nom du relecteur (anonymat) |
| **Étudiant** | Choisir son nom dans une liste (pas de mot de passe — Q1)<br>Saisir un code de présence → présence enregistrée (source = ETUDIANT)<br>Déposer le lien de son exercice pour une session ouverte<br>Remplacer son lien tant qu'aucune relecture n'a commencé (Q13)<br>Être assigné comme relecteur d'un pair présent à la session (Q7)<br>Consulter sa propre note + commentaire (anonyme — Q8) | Se relire lui-même (Q5 — RG2)<br>Marquer sa présence après expiration du code (15 min — Q2, Q3 — RG1)<br>Déposer un exercice après clôture de session (Q12)<br>Voir qui l'a relu |
| **Relecteur** | *N'est PAS un acteur distinct* : c'est un **étudiant dans l'état « assigné à une relecture »**<br>Accéder à l'exercice à relire (lien)<br>Soumettre une note (entier 0–20 — Q9, RG3) + commentaire<br>Modifier sa note/commentaire tant que session non clôturée (Q10) | Noter son propre exercice (RG2)<br>Soumettre une deuxième relecture pour le même exercice (Q6 — RG4)<br>Modifier après clôture session (Q15 tranché vs Q10 — voir section 7) |

> **Décision modélisation :** Le relecteur n'est pas une entité séparée. La table `Relecture` référence `etudiantId` (le relecteur) et `exerciceId` (l'exercice relu). Un étudiant devient relecteur par assignation système.

---

## 3. Périmètre

**Inclus dans cette version (v1.0) :**
- Gestion des promotions et étudiants (données de référence, chargement démo au démarrage)
- Ouverture session par formateur → génération code unique + expiration auto (15 min)
- Marquage présence étudiant via code (contrôle expiration, unicité, blocage 5 essais/2 min — Q4)
- Présence manuelle formateur (traçabilité source = FORMATEUR — Q14)
- Dépôt exercice (lien URI) par étudiant pour une session (jusqu'à clôture — Q12)
- Remplacement lien tant que relecture non commencée (Q13)
- Assignation aléatoire d'un relecteur parmi les **présents à la session** (Q7)
- Relecture : note entière 0–20 + commentaire (Q9, RG3)
- Modification relecture tant que session non clôturée (Q10 — décision section 7)
- Visibilité note + commentaire pour l'étudiant relu, anonymat relecteur (Q8)
- Tableau formateur : par étudiant → nb présences, nb exercices déposés, moyenne notes, nb relectures en attente (Q16)
- Statuts exercice : `DEPOSE` → `EN_ATTENTE_RELECTURE` → `RELU` (bonus D4)
- API REST conforme contrat imposé + extensions nécessaires
- 3 écrans frontend : Formateur (session + tableau), Étudiant (présence + dépôt), Relecture (formulaire note)
- Données de démo préchargées (promotion, 5 étudiants, 1 session ouverte)
- Migrations BDD versionnées (Flyway), tests (unitaire + intégration)

**Explicitement exclu (hors périmètre v1.0) :**
- Authentification / gestion comptes (Q1 : pas de mot de passe, liste noms)
- Gestion multi-promotions simultanées pour un formateur (une session = une promotion)
- Notifications temps réel (WebSocket, push, email) — polling côté frontend suffisant
- Interface d'administration (création promotions/étudiants) — données fixes démo
- Historique complet des sessions passées (seule la session courante gérée)
- Export PDF/Excel du tableau formateur
- Gestion des conflits d'assignation (ex: un seul présent → pas de pair possible) — cas non géré v1
- Récupération exercice si relecteur ne rend jamais (Q11 : reste « en attente » visible au tableau)
- API de suppression / annulation (sauf remplacement lien avant relecture)

---

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|-----|----------|----------------------|----------|
| **EF1** | Le formateur ouvre une session et obtient un code de présence | Quand je saisis un titre et une promotionId, l'API renvoie 201 avec `{id, code, ouvertureAt, expirationAt}` où `expirationAt = ouvertureAt + 15 min` | Must |
| **EF2** | L'étudiant marque sa présence avec un code | Quand je saisis un code valide, non expiré, et mon etudiantId, l'API renvoie 201 avec `{id, sessionId, etudiantId, source: "ETUDIANT"}` et ma présence apparaît au tableau formateur | Must |
| **EF3** | Le formateur ajoute une présence manuelle | Quand le formateur appelle l'endpoint manuel avec sessionId + etudiantId, l'API renvoie 201 avec `source: "FORMATEUR"` et la présence apparaît au tableau | Must |
| **EF4** | L'étudiant dépose le lien de son exercice pour une session | Quand je fournis sessionId, etudiantId, lien (URI valide), l'API renvoie 201 `{id, statut: "DEPOSE"}` et l'exercice apparaît dans mes dépôts | Must |
| **EF5** | L'étudiant remplace son lien d'exercice tant qu'aucune relecture n'a commencé | Quand je redépose un lien pour le même (sessionId, etudiantId) et que le statut est `DEPOSE`, l'API met à jour le lien et renvoie 200 | Should |
| **EF6** | Le système assigne un relecteur aléatoire parmi les présents à la session | Quand un exercice passe en `EN_ATTENTE_RELECTURE`, un étudiant présent (présence enregistrée) et différent de l'auteur est assigné ; la relecture apparaît dans ses tâches | Must |
| **EF7** | Le relecteur soumet une note (0–20 entier) et un commentaire | Quand je fournis note ∈ [0,20] ∩ ℤ et commentaire non vide, l'API renvoie 200 et le statut exercice passe `RELU` | Must |
| **EF8** | Le relecteur modifie sa note/commentaire tant que la session n'est pas clôturée | Quand je soumets à nouveau sur le même id relecture et session ouverte, l'API renvoie 200 avec les nouvelles valeurs | Must |
| **EF9** | L'étudiant relu consulte sa note et le commentaire (anonyme) | Quand je consulte mon exercice relu, l'API renvoie note + commentaire sans identifiant du relecteur | Must |
| **EF10** | Le formateur voit le tableau récapitulatif de sa promotion | Quand j'appelle `GET /api/tableau?promotionId=X`, l'API renvoie 200 avec un tableau par étudiant : `etudiantId, nom, presences, exercicesDeposes, moyenne (null si aucune), relecturesEnAttente` | Must |
| **EF11** | Blocage temporaire après 5 codes erronés (anti-bruteforce) | Quand un étudiant fait 5 tentatives avec code inconnu sur la même session, il reçoit 429 pendant 2 min avant de pouvoir retenter | Should |
| **EF12** | Clôture de session par le formateur | Quand le formateur clôture, plus aucune présence, dépôt, relecture ni modification n'est possible ; assignations figées | Must |

> **Note :** Les endpoints EF3, EF5, EF6, EF11, EF12 ne sont pas dans le contrat imposé → ils seront ajoutés dans `api/contrat.yaml` (section 1.5 de l'étape 1).

---

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|-----|----------|----------------------|
| **ENF1** | Interface marquage présence utilisable sur téléphone (mobile-first) | Test manuel : affichage correct sur viewport 375px, champs saisie code + liste noms accessibles au pouce, feedback visuel clair (succès/erreur) |
| **ENF2** | Tableau formateur répond en < 2 s pour promotion 60 étudiants | Test de charge : `GET /api/tableau?promotionId=1` avec 60 étudiants, 3 sessions, 180 exercices → temps réponse < 2000 ms (mesuré via Spring Boot Actuator / Postman) |
| **ENF3** | Disponibilité : l'application démarre en < 30 s sur poste vierge | Chronomètre : `docker compose up` ou 3 commandes README → Swagger UI accessible + frontend chargé |
| **ENF4** | Aucune règle métier dupliquée côté frontend | Revue de code : calcul moyenne, vérification expiration, unicité présence uniquement côté backend ; frontend appelle API et affiche |
| **ENF5** | Format d'erreur uniforme JSON `{code, message}` pour TOUTES les erreurs (B4) | Test d'intégration : chaque endpoint renvoie le format imposé pour 400, 403, 409, 410, 404, 429 — jamais de stack trace |
| **ENF6** | Schéma BDD versionné, reproductible, sans `ddl-auto=update` (B5) | `./mvnw flyway:migrate` sur BDD vide → schéma complet créé ; `ddl-auto=validate` passe |
| **ENF7** | Deux tests automatisés significatifs (B6) | `./mvnw test` passe : 1 test unitaire règle métier (ex: expiration code), 1 test intégration endpoint (ex: POST /api/presences cas nominal + 410 + 409) |

---

## 6. Règles de gestion

| Réf | Règle | Source |
|-----|-------|--------|
| **RG1** | Un code de présence expire 15 minutes après l'ouverture de la session (`expirationAt = ouvertureAt + 15 min`) | Q2 |
| **RG2** | Un étudiant ne peut pas relire son propre exercice | Q5 |
| **RG3** | Une note est un entier compris entre 0 et 20 inclus | Q9 |
| **RG4** | Un seul relecteur par exercice | Q6 |
| **RG5** | Le relecteur est choisi aléatoirement parmi les étudiants **présents à cette session** | Q7 |
| **RG6** | Un étudiant ne peut marquer sa présence qu'une seule fois par session (unicité session+étudiant) | Q3, contrat 409 DEJA_PRESENT |
| **RG7** | Un étudiant ne peut déposer qu'un seul exercice par session (unicité session+étudiant) | Contrat 409 EXERCICE_DEJA_DEPOSE |
| **RG8** | Le lien d'exercice doit être une URI valide | Contrat 400 LIEN_INVALIDE |
| **RG9** | Le remplacement du lien est autorisé tant que la relecture n'a pas commencé (statut `DEPOSE`) | Q13 |
| **RG10** | Le dépôt d'exercice est possible après la fin de la session tant qu'elle n'est pas clôturée | Q12 |
| **RG11** | La présence manuelle par le formateur est tracée avec `source = FORMATEUR` | Q14, contrat champ `source` enum |
| **RG12** | Après 5 erreurs de code sur une même session, l'étudiant est bloqué 2 minutes (429) | Q4 |
| **RG13** | La note est modifiable par le relecteur tant que la session n'est pas clôturée | Q10 (décision section 7) |
| **RG14** | Une fois la session clôturée, plus aucune modification n'est possible (présences, dépôts, relectures, notes) | Q10, Q15, cohérence métier |
| **RG15** | L'étudiant relu voit la note et le commentaire, mais pas l'identité du relecteur | Q8 |
| **RG16** | Si le relecteur ne rend jamais sa relecture, l'exercice reste en statut `EN_ATTENTE_RELECTURE` visible au tableau | Q11 |
| **RG17** | Le code de présence est unique par session (généré à l'ouverture) | Contrat POST /api/sessions → code |
| **RG18** | La moyenne au tableau est calculée côté backend (pas de recalcul frontend) | F3, contrat réponse `moyenne` |

---

## 7. Zones d'ombre, hypothèses et contradictions

### 7.1 Points non tranchés par le client

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|-------|----------------------------------|------------------|-------------|
| Que se passe-t-il si un seul étudiant est présent à la session ? | Aucune réponse (trou) | **L'assignation relecteur échoue silencieusement** : l'exercice reste en `DEPOSE`, pas de relecture créée. Le formateur le voit comme « en attente » (relecturesEnAttente = 0 car pas de relecteur assignable). Documenté dans l'API (409 si tentative manuelle). | Évite une erreur 500 ; le formateur voit l'exercice non relu. À améliorer v2 (auto-assignation formateur). |
| Que se passe-t-il si le formateur clôture la session alors que des relectures sont en attente ? | Q11 : « reste en attente » + Q10/Q15 contradictoires | **Clôture verrouille tout** : plus de modification notes, plus d'assignation. Les relectures en attente restent `EN_ATTENTE_RELECTURE` figées. La moyenne au tableau ne les compte pas (null). | Cohérent avec RG14. Le formateur voit le nombre `relecturesEnAttente` > 0. |
| Un étudiant peut-il être relecteur de plusieurs exercices dans la même session ? | Non précisé (Q6 : « un seul » par exercice) | **Oui**, un étudiant peut relire plusieurs exercices distincts (un par exercice). L'assignation aléatoire (Q7) le permet. | Modèle : `Relecture` a `etudiantId` (relecteur) + `exerciceId` — unicité sur `(exerciceId)` seulement. |
| Le formateur peut-il rouvrir une session clôturée ? | Non précisé | **Non** — action irréversible v1.0. Nécessiterait une nouvelle session. | Simplifie le cycle de vie. |
| Que devient le code de présence après expiration ? | Q2 : « il ne marche plus » | **Le code reste en BDD** pour traçabilité, mais l'endpoint `/api/presences` renvoie 410 CODE_EXPIRE. | Audit possible. |
| L'étudiant voit-il les sessions pour lesquelles il n'est pas dans la promotion ? | Non précisé | **Non** : l'étudiant ne choisit que parmi les sessions de sa promotion (via promotionId de l'étudiant). | Sécurité / simplicité. |

### 7.2 Contradictions relevées

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---------------------|-------------------|----------|
| **Q10** : « Un relecteur peut corriger sa note après l'avoir envoyée, tant que le formateur n'a pas clôturé la session »<br>**vs**<br>**Q15** : « La note est définitive une fois envoyée. Une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir. » | **Q10 l'emporte sur Q15** | - Q11 décrit un usage concret : « L'exercice reste en attente et je dois le voir clairement dans mon tableau » → implique que le formateur peut attendre avant de clôturer, et pendant ce temps le relecteur peut encore agir.<br>- Q15 est une intention générale (« c'est plus honnête »), pas une règle opérationnelle.<br>- Le contrat API impose `POST /api/relectures/{id}` avec 409 RELECTURE_DEJA_RENDUE **mais** ne prévoit pas de verrouillage définitif avant clôture.<br>- **RG13** = note modifiable tant que session ouverte. **RG14** = clôture verrouille tout. |

> Cette décision impacte : modèle `Relecture` (pas de champ `valideeDefinitivement`), endpoint `POST /api/relectures/{id}` (autorise PUT/PATCH ou POST répété tant que session ouverte), frontend (bouton « Modifier » visible jusqu'à clôture).

---

## 8. Contraintes techniques

### 8.1 Contraintes imposées par le sujet (reprises)

**Backend — Spring Boot (B1–B6)**
| # | Contrainte | Application |
|---|------------|-------------|
| B1 | Java 17+, Maven, wrapper `mvnw` commité | `java.version=17`, `mvnw`/`mvnw.cmd` à la racine `backend/` |
| B2 | Contrat `api/contrat.yaml` respecté à la lettre | Chemins, verbes, codes HTTP, format erreur `{code, message}` identiques |
| B3 | Séparation couches Controller / Service / Repository + DTO | Aucune entité JPA en JSON ; `*Controller` → `*Service` → `*Repository` |
| B4 | Validation entrées + `@RestControllerAdvice` centralisé | `@Valid` sur DTO, `GlobalExceptionHandler` renvoie format imposé |
| B5 | Schéma versionné Flyway, migrations commitées, `ddl-auto=none` | `src/main/resources/db/migration/V1__init.sql`, `V2__...` ; `spring.jpa.hibernate.ddl-auto=validate` |
| B6 | 2 tests : 1 unitaire règle métier + 1 intégration endpoint | `PresenceServiceTest` (expiration), `PresenceControllerIT` (POST /api/presences) |

**Frontend (F1–F3)**
| # | Contrainte | Application |
|---|------------|-------------|
| F1 | Framework déclaré + justifié en 1 ligne dans README | Fait : « React, parce que composants réutilisables, écosystème mature, hooks natifs, Vite » |
| F2 | 3 écrans : Formateur (session + tableau), Étudiant (présence + dépôt), Relecteur (relecture) | Routes : `/formateur`, `/etudiant`, `/relecture/:id` |
| F3 | Couche API dédiée, états chargement/erreur, pas de règle métier dupliquée | `src/api/client.ts` + `useQuery`/`useMutation` (TanStack Query) ; moyenne affichée = `data.moyenne` |

### 8.2 Choix techniques propres

| Domaine | Choix | Justification |
|---------|-------|---------------|
| **Base de données** | H2 (embedded) pour démo/test ; PostgreSQL en prod (profil `prod`) | H2 zero-config pour correcteur ; Flyway compatible les deux |
| **Migrations** | Flyway (SQL natif) | Contrôle total, versionné, rollback manuel si besoin |
| **Build** | Maven (backend), Vite + npm (frontend) | Standards, wrapper commité |
| **Tests** | JUnit 5 + Mockito (unitaire), SpringBootTest + Testcontainers (intégration) | B6 respecté ; Testcontainers pour vraie BDD |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) sur `/swagger-ui.html` | Contrat `api/contrat.yaml` source de vérité ; généré à partir du code pour cohérence |
| **Frontend State** | TanStack Query (React Query) pour serveur + Zustand (client) | Séparation état serveur/client, cache, invalidation auto |
| **UI** | CSS Modules + variables CSS (pas de framework UI lourd) | Contrainte : « rendu visuel non noté, aucun point CSS » → léger, contrôlé |
| **Démarrage** | `docker compose up` (backend + frontend + BDD) **OU** 3 commandes README | Testé depuis clone vierge ; données démo via `CommandLineRunner` |

---

## 9. Livrables

1. **Dépôt GitHub public** : `kfokam48-epreuve-KF48-YOA-270`
   - Structure imposée : `/docs`, `/api`, `/backend`, `/frontend`
   - Historique Git propre : commits atomiques, messages conventionnels, 3 jalons `[JALON]`
   - `.gitignore` Java + Node commité en premier
   - Aucune secret, aucun fichier généré (`target/`, `node_modules/`, `dist/`)
2. **Documentation** (`/docs`)
   - `CAHIER_DES_CHARGES.md` (ce document, maintenu à jour post-étape 3)
   - `JOURNAL.md` (6 entrées, une par étape, rédigées au fil de l'eau)
   - `diagrammes/` : `D1_cas_utilisation.md`, `D2_modele_donnees.md`, `D3_sequence_presence.md`, `D4_etats_exercice.md` (bonus)
3. **Contrat API** (`/api/contrat.yaml`) : 5 opérations imposées + extensions (manuel, clôture, assignation, etc.), figé avant premier commit code
4. **Backend** (`/backend`) : Spring Boot 3, Java 17, Maven, `mvnw`, Flyway, tests, données démo
5. **Frontend** (`/frontend`) : React 18 + Vite, 3 écrans, build passant (`npm run build`)
6. **README.md** : installation testée depuis clone vierge, commandes exactes, justification frontend
7. **CHANGELOG.md** : cohérent avec l'historique Git (étape 4)
8. **SOUMISSION.md** : téléversé sur plateforme avant 18h00 avec 2 URLs + 2 hash 40 chars

---

## 10. Démarche prévue

### Ordre des 6 étapes (respecté scrupuleusement)

| Étape | Objectif | Livrable clé | Jalon Git |
|-------|----------|--------------|-----------|
| **1. Analyse** | Cahier des charges complet, 3 diagrammes Mermaid, backlog issues, contrat API complété | `docs/CAHIER_DES_CHARGES.md` v1, `docs/diagrammes/*.md`, issues GitHub, `api/contrat.yaml` v1 | `[JALON] analyse` (commit vide, **avant** tout code) |
| **2. v0.1** | Implémenter tous les tickets **Must** : backend (5 endpoints + règles), frontend (3 écrans), intégration, tests, données démo | Application fonctionnelle v0.1, démarrage 3 commandes | `[JALON] v0.1` |
| **3. Enveloppe** | Exécuter `./enveloppe`, lire bug + changement, ouvrir issue, reproduire, migrer BDD, maj contrat/docs, séparer correctif/évolution | Code adapté, docs à jour, migration versionnée | (pas de jalon, mais commits traçants) |
| **4. v1.0** | Finaliser Should/Could, nettoyer backlog, `CHANGELOG.md`, `README` testé clone vierge | Version finale livrable | `[JALON] v1.0` |
| **5. Git Lab** | Cloner `git-lab.bundle`, résoudre 5 situations, pousser sur `kfokam48-gitlab-KF48-YOA-270` (public) | 2e dépôt public avec toutes branches | (indépendant) |
| **6. Soumission** | Remplir `SOUMISSION.md`, vérifier liens/hash en navigation privée, téléverser avant 18h00 | Fichier sur plateforme | — |

### Gestion des risques / Plan B si retard

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Enveloppe (étape 3) casse le schéma BDD | Élevée (sujet le dit) | Bloquant si pas versionné | **Flyway dès jour 1** ; migrations commitées avant v0.1 |
| Frontend prend du temps | Moyenne | Retard v0.1 | Composants réutilisables ; API d'abord (contract-first) ; mock MSW si backend pas prêt |
| Tests B6 non passants | Faible | -5 à -10 pts | Écrire test unitaire **en même temps** que la règle (TDD léger) |
| Conflit Q10/Q15 mal tranché | Faible | Incohérence docs/code | Décision documentée section 7, cohérence RG13/RG14, revue code |
| Oubli jalon ou ordre Git | Faible | Malus -5 à -10 | Checklist visuelle dans Journal ; `git log --oneline` avant chaque push |

### Definition of Done — Un ticket est terminé quand :

- [ ] Code implémenté (backend + frontend si concerné)
- [ ] Test unitaire **ou** d'intégration ajouté et passant (`./mvnw test` / `npm test`)
- [ ] Documentation mise à jour si API changée (`api/contrat.yaml`, Swagger)
- [ ] Pas de régression : tous les tests existants passent
- [ ] Revue de code (auto-revue via PR) : respect B3/B4/F3, pas de `console.log`, pas de `System.out`
- [ ] Issue GitHub fermée par le commit de merge (`Closes #X`)
- [ ] Branch de feature supprimée (locale et remote)
- [ ] Entrée `JOURNAL.md` mise à jour si étape terminée

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---------|-------|----------------------------|
| 1 | 25/09/2026 | Version initiale — analyse complète basée sur SUJET.md, CLIENT.md, contrat.yaml. Décision Q10 > Q15 documentée. Trou « seul présent » identifié. |

> **Rappel :** L'étape 3 rendra une partie de ce document faux. Revenir le corriger **immédiatement** après ouverture de l'enveloppe et noter ici — un cahier des charges périmé est un cahier des charges mort.