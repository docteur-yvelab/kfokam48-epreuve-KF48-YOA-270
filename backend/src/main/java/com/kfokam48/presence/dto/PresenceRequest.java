package com.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceRequest {
    @NotBlank
    private String code;

    @NotNull
    private Long etudiantId;
}