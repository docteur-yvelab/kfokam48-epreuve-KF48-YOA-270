package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelectureRepository extends JpaRepository<Relecture, Long> {
    Optional<Relecture> findByExerciceId(Long exerciceId);
    List<Relecture> findByRelecteurId(Long relecteurId);
    boolean existsByExerciceId(Long exerciceId);
}