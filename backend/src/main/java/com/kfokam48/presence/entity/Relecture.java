package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "relecture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false, unique = true)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Column(nullable = false)
    @Min(0)
    @Max(20)
    private Integer note;

    @Column(nullable = false, length = 2000)
    private String commentaire;

    @Column(name = "date_soumission", nullable = false)
    private LocalDateTime dateSoumission = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime dateModification;
}