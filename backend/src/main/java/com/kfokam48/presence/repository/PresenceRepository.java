package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {
    Optional<Presence> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    List<Presence> findBySessionId(Long sessionId);
    long countBySessionId(Long sessionId);
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}