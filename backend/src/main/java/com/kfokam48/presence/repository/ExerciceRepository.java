package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Exercice;
import com.kfokam48.presence.entity.Exercice.StatutExercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    List<Exercice> findBySessionId(Long sessionId);
    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    List<Exercice> findAllByEtudiantIdIn(List<Long> etudiantIds);
    
    @Query("SELECT e FROM Exercice e WHERE e.session.id = :sessionId AND e.statut = :statut AND e.id NOT IN (SELECT r.exercice.id FROM Relecture r)")
    List<Exercice> findExercicesSansRelecteur(Long sessionId, StatutExercice statut);
}