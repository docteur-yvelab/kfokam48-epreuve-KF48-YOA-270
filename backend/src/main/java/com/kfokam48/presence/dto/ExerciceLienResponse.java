package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciceLienResponse {
    private Long id;
    private String lien;
    private LocalDateTime dateModifLien;
}
