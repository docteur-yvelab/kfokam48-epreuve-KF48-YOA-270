package com.kfokam48.presence.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtudiantResponse {
    private Long id;
    private String nom;
    private String prenom;
}
