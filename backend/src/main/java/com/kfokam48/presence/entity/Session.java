package com.kfokam48.presence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private LocalDateTime ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expirationAt;

    @Column(name = "cloture_at")
    private LocalDateTime clotureAt;

    @Column(name = "cloturee", nullable = false)
    private Boolean cloturee = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    private List<Presence> presences;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    private List<Exercice> exercices;

    public boolean isExpiree() {
        return LocalDateTime.now().isAfter(expirationAt);
    }

    public boolean isCloturee() {
        return Boolean.TRUE.equals(cloturee);
    }
}