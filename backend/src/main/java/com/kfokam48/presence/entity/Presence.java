package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "presence", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure = LocalDateTime.now();

    @Column(name = "source", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SourcePresence source;

    public enum SourcePresence {
        ETUDIANT, FORMATEUR
    }
}