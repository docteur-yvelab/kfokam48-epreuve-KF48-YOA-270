# D4 — États-transitions : Cycle de vie d'un exercice (BONUS +3 pts)

```mermaid
stateDiagram-v2
    direction LR
    
    [*] --> DEPOSE : Étudiant dépose lien\nPOST /api/exercices
    
    DEPOSE --> EN_ATTENTE_RELECTURE : Assignation relecteur\n(système, aléatoire parmi présents)
    DEPOSE --> DEPOSE : Remplacement lien autorisé\nPUT /api/exercices/{id}\n(Q13, RG9)
    
    EN_ATTENTE_RELECTURE --> RELU : Relecteur soumet note+commentaire\nPOST /api/relectures/{id}\n(RG3, RG4, RG7)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : Relecteur modifie note\n(jusqu'à clôture, RG13)
    
    RELU --> RELU : Modification note possible\n(tant que session non clôturée, RG13)
    
    DEPOSE --> [*] : Session clôturée\nsans relecture assignée\n(RG14, Q11)
    EN_ATTENTE_RELECTURE --> [*] : Session clôturée\nrelecture non rendue\n(RG14, Q11)
    RELU --> [*] : Session clôturée\n(RG14)
    
    note right of DEPOSE
        Statut initial
        Lien modifiable (RG9)
        1 seul par session/étudiant (RG7)
        Lien URI valide (RG8)
    end note
    
    note right of EN_ATTENTE_RELECTURE
        Relecteur assigné (RG5)
        Pas auto-relecture (RG2)
        1 seul relecteur/exercice (RG4)
        Visible au tableau formateur (Q11)
    end note
    
    note right of RELU
        Note entier 0-20 (RG3)
        Commentaire requis
        Anonyme pour l'étudiant relu (RG15)
        Moyenne calculée backend (RG18)
    end note
    
    note bottom of [*]
        Clôture session (RG14) = verrouillage total
        Plus de dépôts, modifications, assignations
        Relectures en attente figées
    end note
```

**Correspondance API :**
| Transition | Endpoint | Condition |
|------------|----------|-----------|
| `[*] → DEPOSE` | `POST /api/exercices` {sessionId, etudiantId, lien} | Session non clôturée (RG10) |
| `DEPOSE → DEPOSE` | `PUT /api/exercices/{id}` {lien} | Statut = DEPOSE (RG9) |
| `DEPOSE → EN_ATTENTE_RELECTURE` | Interne (système) | Assignation auto au dépôt si pairs présents |
| `EN_ATTENTE_RELECTURE → RELU` | `POST /api/relectures/{id}` {note, commentaire} | Relecteur assigné, note 0-20 (RG3) |
| `RELU → RELU` | `POST /api/relectures/{id}` {note, commentaire} | Session non clôturée (RG13) |
| `→ [*]` | `PATCH /api/sessions/{id}/cloturer` | Formateur seulement |

**Règles de gestion par état :**
| État | Règles applicables |
|------|-------------------|
| `DEPOSE` | RG7, RG8, RG9, RG10 |
| `EN_ATTENTE_RELECTURE` | RG2, RG4, RG5, RG11 (visible), RG16 |
| `RELU` | RG3, RG13, RG15, RG18 |
| Tous (clôture) | RG14 |

**Cas particulier (trou identifié §7.1) :** Si un seul étudiant présent → pas de pair possible → transition `DEPOSE → EN_ATTENTE_RELECTURE` **ne se déclenche pas**, exercice reste en `DEPOSE` jusqu'à clôture.

**Bonus :** Ce diagramme complète D2 (modèle statique) en montrant la dynamique du statut `Exercice.statut` (enum `StatutExercice`).