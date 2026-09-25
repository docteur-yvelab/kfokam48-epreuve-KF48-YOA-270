package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercice", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Exercice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 500)
    private String lien;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private StatutExercice statut = StatutExercice.DEPOSE;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot = LocalDateTime.now();

    @Column(name = "date_modif_lien")
    private LocalDateTime dateModifLien;

    @OneToMany(mappedBy = "exercice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordreRelecteur ASC")
    @Builder.Default
    private List<Relecture> relectures = new ArrayList<>();

    public enum StatutExercice {
        DEPOSE, EN_ATTENTE_RELECTURE, RELU
    }
}