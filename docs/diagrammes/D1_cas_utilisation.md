# D1 — Diagramme de cas d'utilisation

```mermaid
useCaseDiagram
    actor Formateur
    actor Etudiant
    actor Systeme

    package "Gestion des sessions" {
        usecase "Ouvrir une session" as UC1
        usecase "Clôturer une session" as UC2
        usecase "Voir le tableau récapitulatif" as UC3
        usecase "Ajouter une présence manuelle" as UC4
    }

    package "Présence" {
        usecase "Marquer sa présence" as UC5
    }

    package "Exercices" {
        usecase "Déposer un exercice" as UC6
        usecase "Remplacer le lien d'exercice" as UC7
    }

    package "Relecture" {
        usecase "Assigner un relecteur (système)" as UC8
        usecase "Soumettre une relecture" as UC9
        usecase "Modifier une relecture" as UC10
        usecase "Consulter sa note" as UC11
    }

    Formateur --> UC1
    Formateur --> UC2
    Formateur --> UC3
    Formateur --> UC4
    Etudiant --> UC5
    Etudiant --> UC6
    Etudiant --> UC7
    Etudiant --> UC9
    Etudiant --> UC10
    Etudiant --> UC11
    Systeme --> UC8
```