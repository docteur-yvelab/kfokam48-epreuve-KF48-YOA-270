# Fonctionnalités complètes — PresenceKFOKAM

> Liste exhaustive, à cocher au fur et à mesure. Mise à jour avec les livrables de
> l'étape 3 (enveloppe : bug de concurrence + changement « double relecture »).

---

## 0. Prérequis

- [ ] `.gitignore` Java + Node posé avant le premier commit de code
- [ ] `mvnw` / `mvnw.cmd` commités (backend)
- [ ] Structure `/docs`, `/api`, `/backend`, `/frontend` en place

---

## 1. Endpoints API

### Imposés par le contrat (forme figée)
- [ ] `POST /api/sessions` → 201 `{id, code, ouvertureAt, expirationAt}`
- [ ] `POST /api/presences` → 201 `{id, sessionId, etudiantId, source}` + 400/409/410
- [ ] `POST /api/exercices` → 201 `{id, statut}` + 400/409
- [ ] `POST /api/relectures/{id}` → 200 + 400/403/409
- [ ] `GET /api/tableau?promotionId=` → 200 array + 404

### Étendus (à ajouter dans `api/contrat.yaml`)
- [ ] `GET /api/sessions/{id}`
- [ ] `POST /api/presences/manuelle`
- [ ] `PATCH /api/sessions/{id}/cloturer`
- [ ] `PUT /api/exercices/{id}` — remplacer le lien
- [ ] `GET /api/exercices/{id}`
- [ ] `GET /api/relectures/en-attente?etudiantId=`

### Modifiés par le changement de besoin (étape 3)
- [ ] `POST /api/relectures/{id}` — la réponse et la logique doivent maintenant gérer **deux relecteurs par exercice**, pas un seul
- [ ] `GET /api/tableau` — la `moyenne` doit refléter la moyenne des deux notes, avec gestion du cas « une seule note rendue »
- [ ] Ajouter un indicateur de **note provisoire** dans la réponse exposant la note d'un exercice (ex. champ `provisoire: true/false` sur l'exercice ou dans le détail relecture)

---

## 2. Règles métier — version initiale (v0.1)

- [ ] RG1 — Code de présence expire 15 min après ouverture
- [ ] RG2 — Interdiction d'auto-relecture
- [ ] RG3 — Note entière 0-20
- [ ] RG6 — Unicité présence par session/étudiant
- [ ] RG7 — Unicité exercice par session/étudiant
- [ ] RG8 — Lien d'exercice = URI valide
- [ ] RG9 — Remplacement du lien tant que statut `DEPOSE`
- [ ] RG10 — Dépôt possible jusqu'à clôture, pas seulement jusqu'à fin de session
- [ ] RG11 — Présence manuelle tracée `source = FORMATEUR`
- [ ] RG12 — Blocage 2 min après 5 codes faux
- [ ] RG13 — Note modifiable tant que session non clôturée
- [ ] RG14 — Clôture verrouille tout
- [ ] RG15 — Anonymat du relecteur pour l'étudiant relu
- [ ] RG16 — Exercice non relu reste visible « en attente »
- [ ] RG17 — Code unique par session
- [ ] RG18 — Moyenne calculée côté backend uniquement
- [ ] Cas « un seul présent » → pas d'assignation, exercice reste `DEPOSE`

## 2bis. Règles métier — modifiées/ajoutées par le changement de besoin (étape 3)

- [ ] **RG4/RG5 remplacées** : un exercice est désormais relu par **deux relecteurs distincts**, chacun choisi aléatoirement parmi les présents, différents de l'auteur et différents l'un de l'autre
- [ ] La note finale affichée = **moyenne des deux notes** quand les deux relectures sont rendues
- [ ] Si une seule des deux relectures est rendue → la note de celle-ci s'affiche, marquée **provisoire**
- [ ] Si aucune relecture n'est rendue → exercice reste « en attente », comme avant
- [ ] La règle d'unicité change : ce n'est plus « un seul relecteur par exercice » mais « un relecteur ne peut relire deux fois le même exercice » (unicité sur `exerciceId + etudiantId`, plus sur `exerciceId` seul)
- [ ] Le tableau formateur (`moyenne`) doit distinguer une moyenne définitive d'une moyenne provisoire
- [ ] Vérifier l'impact sur RG13 (modification de note) et RG14 (clôture) avec deux relecteurs indépendants

---

## 3. Correction de bug — concurrence sur le marquage de présence

> Symptôme rapporté : deux étudiants tapent le même code presque simultanément,
> un seul apparaît dans la liste du formateur. Au deuxième essai, les deux passent.
> C'est une **race condition** sur l'insertion de la présence (contrainte d'unicité
> non gérée correctement sous accès concurrent, ou vérification puis insertion non atomique).

- [ ] Issue ouverte décrivant le bug + étapes de reproduction, **avant tout commit de correction**
- [ ] Test qui reproduit la concurrence et échoue (ex. deux requêtes `POST /api/presences` simultanées sur le même code/session mais étudiants différents → les deux doivent réussir)
- [ ] Correction : rendre l'opération atomique (contrainte d'unicité BDD correcte sur `(sessionId, etudiantId)` + gestion propre de l'exception de violation de contrainte, transaction bien isolée)
- [ ] Le test passe après correction
- [ ] Commit de correction sur une branche dédiée, référence l'issue (`Fixes #X`)

---

## 4. Changement de besoin — double relecture (étape 3)

- [ ] Nouvelle issue ouverte décrivant le changement, séparée de celle du bug
- [ ] `docs/CAHIER_DES_CHARGES.md` mis à jour : section 6 (RG4/RG5 remplacées), section 4 (EF6/EF7 reformulées), section 7 (nouvelle zone d'ombre : que faire si les deux notes sont très différentes ? aucune règle de tolérance donnée par le client)
- [ ] Diagrammes `D2_modele_donnees.md` et `D3_sequence_presence.md` (ou nouveau diagramme séquence relecture) mis à jour
- [ ] `api/contrat.yaml` mis à jour (réponses relecture/tableau)
- [ ] **Nouvelle migration Flyway** (ex. `V2__double_relecture.sql`) — jamais de modification de `V1`, la base existante doit survivre à la migration
- [ ] Re-priorisation écrite dans le journal ou le cahier des charges : qu'est-ce qui sort du périmètre pour absorber ce Must tardif ?
- [ ] Deux branches distinctes, deux PR distinctes : une pour le bug, une pour le changement
- [ ] Entrée `docs/JOURNAL.md` — Étape 3 remplie, avec la section « Ce que j'ai sorti du périmètre »

---

## 5. Frontend — 3 écrans

**Formateur**
- [ ] Ouvrir session, afficher code + expiration
- [ ] Tableau récapitulatif (avec distinction moyenne définitive / provisoire après le changement)
- [ ] Ajouter présence manuelle
- [ ] Clôturer la session

**Étudiant**
- [ ] Choisir son nom (pas de mot de passe)
- [ ] Saisir code → feedback succès/erreur/expiré/bloqué
- [ ] Déposer lien d'exercice
- [ ] Remplacer son lien tant que non relu
- [ ] Voir sa note (+ mention "provisoire" si un seul relecteur a rendu) et le commentaire, sans identité du relecteur

**Relecteur**
- [ ] Liste des relectures en attente
- [ ] Formulaire note (0-20) + commentaire
- [ ] Modifier une relecture déjà envoyée tant que session ouverte

**Transversal**
- [ ] Couche API dédiée
- [ ] États de chargement/erreur gérés
- [ ] Responsive / mobile-first pour les écrans étudiant

---

## 6. Base de données

- [ ] Entités : Promotion, Étudiant, Session, Presence, Exercice, Relecture
- [ ] Migration `V1` initiale (schéma de base)
- [ ] Migration `V2` (étape 3) : passage à deux relecteurs, contrainte d'unicité corrigée, colonne provisoire si nécessaire
- [ ] `ddl-auto=validate` (jamais `update`)
- [ ] Données de démo au démarrage

---

## 7. Tests

- [ ] 1 test unitaire règle métier (ex. calcul d'expiration ou interdiction auto-relecture)
- [ ] 1 test intégration endpoint (ex. `POST /api/presences` nominal + 410 + 409)
- [ ] Test de concurrence démontrant le bug corrigé (étape 3)
- [ ] Test(s) couvrant la nouvelle règle des deux relecteurs et la note provisoire

---

## 8. Documentation

- [ ] `docs/CAHIER_DES_CHARGES.md` (10 sections + journal des révisions mis à jour à l'étape 3)
- [ ] `docs/JOURNAL.md` (6 entrées, dont Étape 3 avec bug + changement + sacrifice de périmètre)
- [ ] `docs/diagrammes/` D1, D2, D3 (mis à jour), D4 (bonus)
- [ ] `api/contrat.yaml` figé puis mis à jour à l'étape 3
- [ ] `README.md` testé depuis clone vierge
- [ ] `CHANGELOG.md` cohérent avec l'historique (étape 4)
- [ ] `SOUMISSION.md` rempli

---

## 9. Git

- [ ] 3 commits `[JALON]` : `analyse` → `v0.1` → `v1.0`, dans l'ordre
- [ ] Une branche par ticket, PR liée à l'issue (`Fixes #X`)
- [ ] Étape 3 : deux branches/PR distinctes (bug ≠ changement), issue avant code, test avant correctif
- [ ] `main` toujours sain
- [ ] Aucun secret, aucun fichier généré commité

---

## 10. Ce qui est noté spécifiquement à l'étape 3 (10 pts)

- [ ] L'issue du bug existe avant le premier commit de correction (visible dans l'ordre de l'historique)
- [ ] Le bug est reproduit par un test qui échoue, avant d'être corrigé
- [ ] La migration est **ajoutée**, jamais modifiée en place
- [ ] L'analyse (cahier des charges + diagrammes) est remise à jour, pas laissée périmée
- [ ] Le sacrifice de périmètre est écrit noir sur blanc
- [ ] Correctif et évolution sont séparés : deux branches, deux PR