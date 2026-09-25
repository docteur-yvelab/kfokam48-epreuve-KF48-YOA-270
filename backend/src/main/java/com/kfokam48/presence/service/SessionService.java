package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.ClotureResponse;
import com.kfokam48.presence.dto.SessionDetailResponse;
import com.kfokam48.presence.dto.SessionRequest;
import com.kfokam48.presence.dto.SessionResponse;
import com.kfokam48.presence.entity.Promotion;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.exception.PromotionInconnueException;
import com.kfokam48.presence.exception.SessionClotureeException;
import com.kfokam48.presence.exception.SessionDejaClotureeException;
import com.kfokam48.presence.exception.SessionInconnueException;
import com.kfokam48.presence.repository.PromotionRepository;
import com.kfokam48.presence.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PromotionRepository promotionRepository;

    public SessionResponse ouvrirSession(SessionRequest request) {
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(PromotionInconnueException::new);

        String code = genererCodeUnique();
        LocalDateTime now = LocalDateTime.now();

        Session session = Session.builder()
                .titre(request.getTitre())
                .code(code)
                .ouvertureAt(now)
                .expirationAt(now.plusMinutes(15))
                .cloturee(false)
                .promotion(promotion)
                .build();

        session = sessionRepository.save(session);
        return toResponse(session);
    }

    public ClotureResponse cloturerSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(SessionInconnueException::new);
        if (session.isCloturee()) {
            throw new SessionDejaClotureeException();
        }
        session.setCloturee(true);
        session.setClotureAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        return ClotureResponse.builder()
                .id(session.getId())
                .cloturee(session.isCloturee())
                .clotureAt(session.getClotureAt())
                .build();
    }

    /** GET /api/sessions?promotionId= (contrat) */
    public List<SessionDetailResponse> listerSessionsPromotion(Long promotionId) {
        promotionRepository.findById(promotionId).orElseThrow(PromotionInconnueException::new);
        return sessionRepository.findByPromotionIdOrderByClotureeAscOuvertureAtDesc(promotionId)
                .stream()
                .map(this::toDetailResponse)
                .toList();
    }

    /** GET /api/sessions/{id} (contrat) */
    public SessionDetailResponse getSession(Long id) {
        return toDetailResponse(sessionRepository.findById(id).orElseThrow(SessionInconnueException::new));
    }

    private SessionDetailResponse toDetailResponse(Session session) {
        return SessionDetailResponse.builder()
                .id(session.getId())
                .titre(session.getTitre())
                .code(session.getCode())
                .ouvertureAt(session.getOuvertureAt())
                .expirationAt(session.getExpirationAt())
                .cloturee(session.isCloturee())
                .promotionId(session.getPromotion() != null ? session.getPromotion().getId() : null)
                .build();
    }

    public Session getSessionOuverteParPromotion(Long promotionId) {
        return sessionRepository.findByPromotionIdAndClotureeFalse(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune session ouverte pour cette promotion"));
    }

    public Session getSessionByCode(String code) {
        return sessionRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable"));
    }

    private String genererCodeUnique() {
        String code;
        Random random = new Random();
        do {
            code = "KF48-" + String.format("%06d", random.nextInt(1_000_000));
        } while (sessionRepository.findByCode(code).isPresent());
        return code;
    }

    private SessionResponse toResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .code(session.getCode())
                .ouvertureAt(session.getOuvertureAt())
                .expirationAt(session.getExpirationAt())
                .build();
    }
}