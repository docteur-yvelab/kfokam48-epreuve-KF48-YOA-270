package com.kfokam48.presence.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelectureRequest {
    @NotNull
    @Min(0)
    @Max(20)
    private Integer note;

    @NotBlank
    private String commentaire;
}