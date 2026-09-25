# D1 — Diagramme de cas d'utilisation

```mermaid
useCaseDiagram
    left to right direction
    
    actor "Formateur" as Prof
    actor "Étudiant" as Etu
    
    package "Gestion des sessions" {
        usecase "Ouvrir une session\n(titre, promotion)" as UC1
        usecase "Clôturer une session" as UC2
        usecase "Voir le tableau récapitulatif" as UC3
        usecase "Ajouter une présence manuelle" as UC4
    }
    
    package "Présence" {
        usecase "Marquer sa présence (code)" as UC5
    }
    
    package "Exercices" {
        usecase "Déposer un exercice (lien)" as UC6
        usecase "Remplacer son lien d'exercice" as UC7
    }
    
    package "Relecture" {
        usecase "Être assigné comme relecteur" as UC8
        usecase "Soumettre une note + commentaire" as UC9
        usecase "Modifier sa relecture" as UC10
        usecase "Consulter sa note (anonyme)" as UC11
    }
    
    Prof --> UC1
    Prof --> UC2
    Prof --> UC3
    Prof --> UC4
    
    Etu --> UC5
    Etu --> UC6
    Etu --> UC7
    Etu --> UC8
    Etu --> UC9
    Etu --> UC10
    Etu --> UC11
    
    note right of UC5 : Code expire 15 min (RG1)\nBlocage 5 essais/2 min (RG12)
    note right of UC6 : Jusqu'à clôture (RG10)\n1 seul par session (RG7)
    note right of UC8 : Aléatoire parmi présents (RG5)\nPas auto-relecture (RG2)
    note right of UC9 : Note entier 0-20 (RG3)\n1 relecteur/exercice (RG4)
    note right of UC10 : Tant que session ouverte (RG13)
    note right of UC11 : Anonymat relecteur (RG15)
```

**Acteurs :** Formateur, Étudiant (le relecteur est un état de l'étudiant, pas un acteur distinct — cf. cahier des charges §2)

**Couverture exigences :** EF1→UC1, EF2→UC5, EF3→UC4, EF4→UC6, EF5→UC7, EF6→UC8, EF7→UC9, EF8→UC10, EF9→UC11, EF10→UC3, EF11→UC5 (règle), EF12→UC2