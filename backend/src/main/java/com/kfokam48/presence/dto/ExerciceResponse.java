package com.kfokam48.presence.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciceResponse {
    private Long id;
    private String statut;
}