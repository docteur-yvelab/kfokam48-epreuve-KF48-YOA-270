# D2 — Modèle de données (classes / entités)

```mermaid
classDiagram
    direction TB
    
    class Promotion {
        +Long id
        +String nom
        +String annee
        +List~Etudiant~ etudiants
    }
    
    class Etudiant {
        +Long id
        +String nom
        +String prenom
        +String email
        +Promotion promotion
        +List~Presence~ presences
        +List~Exercice~ exercices
        +List~Relecture~ relecturesFaites
    }
    
    class Session {
        +Long id
        +String titre
        +String code
        +LocalDateTime ouvertureAt
        +LocalDateTime expirationAt
        +LocalDateTime clotureAt
        +Boolean cloturee
        +Promotion promotion
        +List~Presence~ presences
        +List~Exercice~ exercices
    }
    
    class Presence {
        +Long id
        +Session session
        +Etudiant etudiant
        +LocalDateTime dateHeure
        +SourcePresence source
        +unique(session, etudiant)
    }
    
    class Exercice {
        +Long id
        +Session session
        +Etudiant etudiant
        +String lien
        +StatutExercice statut
        +LocalDateTime dateDepot
        +LocalDateTime dateModifLien
        +Relecture relecture
        +unique(session, etudiant)
    }
    
    class Relecture {
        +Long id
        +Exercice exercice
        +Etudiant relecteur
        +Integer note
        +String commentaire
        +LocalDateTime dateSoumission
        +LocalDateTime dateModification
        +unique(exercice)
    }
    
    class SourcePresence {
        <<enumeration>>
        ETUDIANT
        FORMATEUR
    }
    
    class StatutExercice {
        <<enumeration>>
        DEPOSE
        EN_ATTENTE_RELECTURE
        RELU
    }
    
    Promotion "1" --> "0..*" Etudiant : etudiants
    Etudiant "*" --> "1" Promotion : promotion
    
    Session "*" --> "1" Promotion : promotion
    Promotion "1" --> "0..*" Session : sessions
    
    Presence "*" --> "1" Session : session
    Session "1" --> "0..*" Presence : presences
    
    Presence "*" --> "1" Etudiant : etudiant
    Etudiant "1" --> "0..*" Presence : presences
    
    Presence --> SourcePresence : source
    
    Exercice "*" --> "1" Session : session
    Session "1" --> "0..*" Exercice : exercices
    
    Exercice "*" --> "1" Etudiant : etudiant (auteur)
    Etudiant "1" --> "0..*" Exercice : exercices
    
    Relecture "1" --> "1" Exercice : exercice
    Exercice "1" --> "0..1" Relecture : relecture
    
    Relecture "*" --> "1" Etudiant : relecteur
    Etudiant "1" --> "0..*" Relecture : relecturesFaites
    
    Exercice --> StatutExercice : statut
```

**Correspondance migrations Flyway (à venir) :**
- `V1__create_schema.sql` : tables `promotion`, `etudiant`, `session`, `presence`, `exercice`, `relecture`
- `V2__add_indexes_constraints.sql` : indexes sur code, unicités, FK
- `V3__insert_demo_data.sql` : 1 promotion, 5 étudiants, 1 session ouverte

**Règles de gestion mappées :**
- RG1 : `Session.expirationAt = ouvertureAt + 15 min`
- RG2 : `Relecture.relecteur != Exercice.etudiant` (contrainte applicative)
- RG3 : `Relecture.note` CHECK (0-20)
- RG4 : `Relecture` unique sur `exercice_id`
- RG5 : Assignation via requête `Etudiant` présent à la `Session`
- RG6 : `Presence` unique sur `(session_id, etudiant_id)`
- RG7 : `Exercice` unique sur `(session_id, etudiant_id)`
- RG11 : `Presence.source` = FORMATEUR pour présence manuelle
- RG16 : `Exercice.statut = EN_ATTENTE_RELECTURE` si pas de relecture