# D2 — Modèle de données (Classes / Entités)

```mermaid
classDiagram
    class Promotion {
        +Long id
        +String nom
        +String annee
    }

    class Etudiant {
        +Long id
        +String nom
        +String prenom
        +String email
        +Long promotionId
    }

    class Session {
        +Long id
        +String titre
        +String code
        +LocalDateTime ouvertureAt
        +LocalDateTime expirationAt
        +LocalDateTime clotureAt
        +Boolean cloturee
        +Long promotionId
        +isExpiree() boolean
        +isCloturee() boolean
    }

    class Presence {
        +Long id
        +Long sessionId
        +Long etudiantId
        +LocalDateTime dateHeure
        +SourcePresence source
        <<enumeration>> SourcePresence { ETUDIANT, FORMATEUR }
    }

    class Exercice {
        +Long id
        +Long sessionId
        +Long etudiantId
        +String lien
        +StatutExercice statut
        +LocalDateTime dateDepot
        +LocalDateTime dateModifLien
        <<enumeration>> StatutExercice { DEPOSE, EN_ATTENTE_RELECTURE, RELU }
    }

    class Relecture {
        +Long id
        +Long exerciceId
        +Long relecteurId
        +Integer note
        +String commentaire
        +LocalDateTime dateSoumission
        +LocalDateTime dateModification
    }

    Promotion "1" --> "*" Etudiant : contient
    Promotion "1" --> "*" Session : organise
    Session "1" --> "*" Presence : enregistre
    Session "1" --> "*" Exercice : reçoit
    Etudiant "1" --> "*" Presence : marque
    Etudiant "1" --> "*" Exercice : dépose
    Exercice "1" --> "0..1" Relecture : relu par
    Etudiant "1" --> "*" Relecture : effectue (en tant que relecteur)

    note for Presence "Unicité : (sessionId, etudiantId)"
    note for Exercice "Unicité : (sessionId, etudiantId)"
    note for Relecture "Unicité : exerciceId (un seul relecteur par exercice)"
```