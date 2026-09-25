package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableauEtudiantResponse {
    private Long etudiantId;
    private String nom;
    private Integer presences;
    private Integer exercicesDeposes;
    private Double moyenne;
    private Integer relecturesEnAttente;
}