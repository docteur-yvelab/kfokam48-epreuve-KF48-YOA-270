package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Session complète : sert à la fois pour GET /api/sessions (liste) et GET /api/sessions/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDetailResponse {
    private Long id;
    private String titre;
    private String code;
    private LocalDateTime ouvertureAt;
    private LocalDateTime expirationAt;
    private Boolean cloturee;
    private Long promotionId;
}
