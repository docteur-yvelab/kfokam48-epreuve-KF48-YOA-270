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
}