package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private Long id;
    private String code;
    private LocalDateTime ouvertureAt;
    private LocalDateTime expirationAt;
}