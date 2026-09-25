package com.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** Corps de PUT /api/exercices/{id} : { "lien": "https://..." } */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LienRequest {
    @NotBlank
    private String lien;
}
