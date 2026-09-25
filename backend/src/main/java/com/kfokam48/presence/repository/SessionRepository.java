package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByCode(String code);
    Optional<Session> findByPromotionIdAndClotureeFalse(Long promotionId);
}