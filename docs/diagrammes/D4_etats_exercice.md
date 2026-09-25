# D4 — États-transitions : Cycle de vie d'un exercice (BONUS +3 pts) — **Version 2 relecteurs**

```mermaid
stateDiagram-v2
    direction LR
    
    [*] --> DEPOSE : Étudiant dépose lien\nPOST /api/exercices
    
    DEPOSE --> EN_ATTENTE_RELECTURE_1 : Assignation relecteur 1\n(système, aléatoire parmi présents)
    DEPOSE --> EN_ATTENTE_RELECTURE_2 : Assignation relecteur 2\n(système, aléatoire parmi présents, ≠ relecteur 1)
    DEPOSE --> DEPOSE : Remplacement lien autorisé\nPUT /api/exercices/{id}\n(Q13, RG9)
    
    EN_ATTENTE_RELECTURE_1 --> RELECTURE_1_RENDUE : Relecteur 1 soumet note+commentaire\nPOST /api/relectures/{exerciceId}\n(RG3, RG4, RG7)
    EN_ATTENTE_RELECTURE_2 --> RELECTURE_2_RENDUE : Relecteur 2 soumet note+commentaire\nPOST /api/relectures/{exerciceId}\n(RG3, RG4, RG7)
    
    RELECTURE_1_RENDUE --> EN_ATTENTE_RELECTURE_2 : Attente relecteur 2\n(moyenneProvisoire = true)
    RELECTURE_2_RENDUE --> EN_ATTENTE_RELECTURE_1 : Attente relecteur 1\n(moyenneProvisoire = true)
    
    EN_ATTENTE_RELECTURE_1 --> RELU : Relecteur 2 soumet\n(les deux notes reçues)
    EN_ATTENTE_RELECTURE_2 --> RELU : Relecteur 1 soumet\n(les deux notes reçues)
    RELECTURE_1_RENDUE --> RELU : Relecteur 2 soumet
    RELECTURE_2_RENDUE --> RELU : Relecteur 1 soumet
    
    RELU --> RELU : Modification note possible\n(tant que session non clôturée, RG13)
    
    DEPOSE --> [*] : Session clôturée\nsans relecture assignée\n(RG14, Q11)
    EN_ATTENTE_RELECTURE_1 --> [*] : Session clôturée\nrelecture 1 non rendue\n(moyenneProvisoire = true)
    EN_ATTENTE_RELECTURE_2 --> [*] : Session clôturée\nrelecture 2 non rendue\n(moyenneProvisoire = true)
    RELECTURE_1_RENDUE --> [*] : Session clôturée\nrelecture 2 non rendue\n(moyenneProvisoire = true)
    RELECTURE_2_RENDUE --> [*] : Session clôturée\nrelecture 1 non rendue\n(moyenneProvisoire = true)
    RELU --> [*] : Session clôturée\n(RG14)
    
    note right of DEPOSE
        Statut initial
        Lien modifiable (RG9)
        1 seul par session/étudiant (RG7)
        Lien URI valide (RG8)
        **2 assignations parallèles** (RG4, RG5)
    end note
    
    note right of EN_ATTENTE_RELECTURE_1
        Relecteur 1 assigné (RG5)
        Pas auto-relecture (RG2)
        ≠ relecteur 2 (RG4)
        **moyenneProvisoire** si note 1 seule
    end note
    
    note right of EN_ATTENTE_RELECTURE_2
        Relecteur 2 assigné (RG5)
        Pas auto-relecture (RG2)
        ≠ relecteur 1 (RG4)
        **moyenneProvisoire** si note 2 seule
    end note
    
    note right of RELECTURE_1_RENDUE
        Note 1 reçue, attente note 2
        **moyenneProvisoire = true**
        Visible au tableau formateur
    end note
    
    note right of RELECTURE_2_RENDUE
        Note 2 reçue, attente note 1
        **moyenneProvisoire = true**
        Visible au tableau formateur
    end note
    
    note right of RELU
        **Moyenne définitive** des 2 notes (arrondie)
        Note entier 0-20 (RG3)
        Commentaires requis
        Anonyme pour l'étudiant relu (RG15)
        Moyenne calculée backend (RG18)
    end note
    
    note bottom of [*]
        Clôture session (RG14) = verrouillage total
        Plus de dépôts, modifications, assignations
        Relectures en attente figées
        Si une seule note → moyenneProvisoire = true
    end note
```

**Correspondance API (2 relecteurs) :**
| Transition | Endpoint | Condition |
|------------|----------|-----------|
| `[*] → DEPOSE` | `POST /api/exercices` {sessionId, etudiantId, lien} | Session non clôturée (RG10) |
| `DEPOSE → DEPOSE` | `PUT /api/exercices/{id}` {lien} | Statut = DEPOSE (RG9) |
| `DEPOSE → EN_ATTENTE_RELECTURE_1` | Interne (système) | Assignation auto relecteur 1 |
| `DEPOSE → EN_ATTENTE_RELECTURE_2` | Interne (système) | Assignation auto relecteur 2 (≠ relecteur 1) |
| `EN_ATTENTE_RELECTURE_1 → RELECTURE_1_RENDUE` | `POST /api/relectures/{exerciceId}` {note, commentaire} + header X-Etudiant-Id | Relecteur 1 assigné, note 0-20 (RG3) |
| `EN_ATTENTE_RELECTURE_2 → RELECTURE_2_RENDUE` | `POST /api/relectures/{exerciceId}` {note, commentaire} + header X-Etudiant-Id | Relecteur 2 assigné, note 0-20 (RG3) |
| `RELECTURE_1_RENDUE → EN_ATTENTE_RELECTURE_2` | Interne | Attente relecteur 2 (moyenneProvisoire) |
| `RELECTURE_2_RENDUE → EN_ATTENTE_RELECTURE_1` | Interne | Attente relecteur 1 (moyenneProvisoire) |
| `→ RELU` | Interne | Les 2 notes reçues |
| `RELU → RELU` | `POST /api/relectures/{exerciceId}` {note, commentaire} | Session non clôturée (RG13), modification par ordreRelecteur |
| `→ [*]` | `PATCH /api/sessions/{id}/cloturer` | Formateur seulement |

**Règles de gestion par état (2 relecteurs) :**
| État | Règles applicables |
|------|-------------------|
| `DEPOSE` | RG7, RG8, RG9, RG10, RG4 (2 assignations), RG5 |
| `EN_ATTENTE_RELECTURE_1` | RG2, RG4 (≠ relecteur 2), RG5, RG19 (moyenneProvisoire) |
| `EN_ATTENTE_RELECTURE_2` | RG2, RG4 (≠ relecteur 1), RG5, RG19 (moyenneProvisoire) |
| `RELECTURE_1_RENDUE` | RG3, RG13, RG19 (moyenneProvisoire=true) |
| `RELECTURE_2_RENDUE` | RG3, RG13, RG19 (moyenneProvisoire=true) |
| `RELU` | RG3, RG13, RG15, RG18, RG19 (moyenne définitive), RG20 |
| Tous (clôture) | RG14, RG20 |

**Cas particulier (trou §7.1) :** Si un seul étudiant présent → pas de pair possible → transition `DEPOSE → EN_ATTENTE_RELECTURE_1/2` **ne se déclenche pas**, exercice reste en `DEPOSE` jusqu'à clôture.

**Bonus :** Ce diagramme complète D2 (modèle statique) en montrant la dynamique du statut `Exercice.statut` (enum `StatutExercice` : DEPOSE, EN_ATTENTE_RELECTURE, RELU) avec 2 relecteurs.