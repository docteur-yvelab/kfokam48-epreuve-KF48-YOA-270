package com.kfokam48.presence.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClotureResponse {
    private Long id;
    private Boolean cloturee;
    private LocalDateTime clotureAt;
}
