package com.kfokam48.presence.repository;

import com.kfokam48.presence.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByCode(String code);
    Optional<Session> findByPromotionIdAndClotureeFalse(Long promotionId);

    /** Sessions d'une promotion : les sessions ouvertes d'abord, puis les plus récentes. */
    List<Session> findByPromotionIdOrderByClotureeAscOuvertureAtDesc(Long promotionId);
}