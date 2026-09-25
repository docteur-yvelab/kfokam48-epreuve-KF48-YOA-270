package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelectureResponse {
    private Long id;
    private Long exerciceId;
    private Short ordreRelecteur;
    private Long relecteurId;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
    private Boolean noteProvisoire;

    /** Lien de l'exercice relu (requis par GET /api/relectures/en-attente du contrat). */
    private String exerciceLien;

    /** Date d'assignation de la relecture (requise par le contrat). */
    private LocalDateTime dateAssignation;
}