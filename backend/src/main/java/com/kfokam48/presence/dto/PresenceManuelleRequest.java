package com.kfokam48.presence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceManuelleRequest {
    @NotNull
    private Long sessionId;

    @NotNull
    private Long etudiantId;
}