package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Détail d'un exercice pour l'écran de relecture.
 * Contient "relecture" (contrat, objet nullable) et "relectures" (les 2 relectures, utilisé par le frontend).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciceDetailResponse {
    private Long id;
    private Long sessionId;
    private Long etudiantId;
    private String lien;
    private String statut;
    private LocalDateTime dateDepot;
    private Integer note;
    private String commentaire;
    private RelectureResponse relecture;
    private List<RelectureResponse> relectures;
}
