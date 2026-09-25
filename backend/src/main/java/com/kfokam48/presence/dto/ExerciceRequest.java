package com.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciceRequest {
    @NotNull
    private Long sessionId;

    @NotNull
    private Long etudiantId;

    @NotBlank
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Lien doit être une URI valide")
    private String lien;
}