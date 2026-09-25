package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceResponse {
    private Long id;
    private Long sessionId;
    private Long etudiantId;
    private String source;
}