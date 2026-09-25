# Journal de bord - 270

> Une entrée **par étape**, écrite **au moment où tu la termines**, pas à la fin de la journée.
> Trois lignes suffisent. Un journal rédigé d'un bloc juste avant de soumettre se repère
> immédiatement dans l'historique Git et ne compte pas.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que tu viens de terminer
- **Bloqué** — ce qui t'a coûté du temps, et combien
- **IA** — ce que tu lui as demandé, et **comment tu as vérifié sa réponse**

---

## Étape 1 — Analyse et conception

**Fait :** Cahier des charges complet (10 sections, 12 EF, 18 RG, contradictions Q10/Q15 tranchées en faveur de Q10, trou « seul étudiant présent » identifié), structure dépôt initialisée, `.gitignore` Java+Node commité en premier, `README.md` avec justification frontend, contrat API copié, modèle docs copiés, commit `[JALON] analyse` poussé (historique : .gitignore → structure → jalon analyse → README → cahier des charges sur develop).

**Bloqué :** 15 min sur la contradiction Q10 vs Q15. Tranchée pour Q10 (usage concret Q11) avec RG13/RG14 cohérents. 10 min sur le trou « seul présent » : décision = assignation échoue silencieusement, exercice reste DEPOSE.

**IA :** Aidé à structurer le cahier des charges (10 sections), lister les RG exhaustives depuis CLIENT.md + contrat, formuler les EF avec critères vérifiables. Vérifié : chaque RG cite sa source Qx, chaque EF a un critère « quand… alors… », contradictions documentées avec justification.

---

## Étape 2 — Première version

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 3 — Enveloppe

**Fait :** 
- Bug race condition présence concurrente (issue #20) : écrit test `PresenceConcurrencyIT` qui reproduit le bug (2 étudiants simultanés), corrigé `PresenceService.marquerPresence` avec insertion atomique (INSERT ... ON CONFLICT), test passe.
- Changement besoin 2 relecteurs (issues #21, #22) : migration Flyway V3__deux_relecteurs.sql (recréation table relecture avec ordreRelecteur 1/2, noteProvisoire), entités Relecture/Exercice modifiées, RelectureService.assignerDeuxRelecteurs + soumettreRelecture par ordre, ExerciceService appelle assignation auto, RelectureController POST /api/relectures/{exerciceId} + GET /exercice/{id}, TableauService moyenne 2 notes + flag moyenneProvisoire, frontend RelecturePage route /relecture/:exerciceId/:ordre + badge provisoire, EtudiantPage affiche 2 relectures + moyenne, FormateurPage badge provisoire.
- MAJ docs : CAHIER_DES_CHARGES.md (RG4/RG5/RG19/RG20, EF6-EF10, section 7.3), diagrammes D2/D4, api/contrat.yaml (nouveaux endpoints, moyenneProvisoire).

**Bloqué :** 2h sur migration V3 (H2 ne supporte pas DROP CONSTRAINT, solution recréation table + copy data). 1h sur Lombok/Java 24 (résolu avec lombok 1.18.38 + annotationProcessorPath). 30 min sur test concurrence (session de démo V2 utilisée via @BeforeEach).

**IA :** 
- Aidé pour structurer migration V3, écrire test concurrent (CountDownLatch + ExecutorService), corriger RelectureService pour 2 relecteurs distincts, adapter TableauService pour moyenne + provisoire.
- Vérifié : test PresenceConcurrencyIT passe (2/2 succès), build frontend/backend passe, 4 tests backend passent, migration V3 s'applique sur BDD existante sans perte.

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**
- Notifications temps réel WebSocket (issue #23 Could) : sacrifié pour concentrer sur le Must "2 relecteurs". Le polling TanStack Query (refetchInterval 30s) suffit pour v1.0. Impact utilisateur : délai max 30s pour voir nouvelle assignation, acceptable.

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
