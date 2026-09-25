# D4 — États-transitions : Cycle de vie d'un exercice (Bonus)

```mermaid
stateDiagram-v2
    [*] --> DEPOSE : Étudiant dépose lien\n(POST /api/exercices)

    state DEPOSE {
        [*] --> AttenteAssignation
        AttenteAssignation --> AttenteAssignation : Système cherche\nrelecteur parmi présents
    }

    DEPOSE --> EN_ATTENTE_RELECTURE : Relecteur assigné\n(aléatoire parmi présents\n- Q7, RG5)
    
    EN_ATTENTE_RELECTURE --> RELU : Relecteur soumet note\n+ commentaire\n(POST /api/relectures/{id})
    
    EN_ATTENTE_RELECTURE --> DEPOSE : Remplacement lien autorisé\n(Q13, RG9)\nsi relecture non commencée
    
    RELU --> EN_ATTENTE_RELECTURE : Modification note/commentaire\n(Q10, RG13)\nTant que session non clôturée
    
    EN_ATTENTE_RELECTURE --> [*] : Session clôturée\n(RG14) - figé en attente
    RELU --> [*] : Session clôturée\n(RG14) - note définitive
    
    DEPOSE --> [*] : Session clôturée\nsans relecteur assigné\n(seul présent - trou)
    
    note right of DEPOSE : Statut initial\nLien modifiable\n(Q13)
    note right of EN_ATTENTE_RELECTURE : Un relecteur assigné\nEn attente de sa note\n(Q11 - visible au tableau)
    note right of RELU : Note + commentaire reçus\nModifiable jusqu'à clôture\n(Q10 vs Q15 tranché)
```