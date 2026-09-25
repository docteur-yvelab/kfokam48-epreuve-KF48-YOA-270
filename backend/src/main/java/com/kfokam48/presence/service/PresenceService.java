package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.PresenceRequest;
import com.kfokam48.presence.dto.PresenceResponse;
import com.kfokam48.presence.dto.PresenceManuelleRequest;
import com.kfokam48.presence.entity.Presence;
import com.kfokam48.presence.entity.Session;
import com.kfokam48.presence.entity.Etudiant;
import com.kfokam48.presence.exception.CodeInconnuException;
import com.kfokam48.presence.exception.CodeExpireException;
import com.kfokam48.presence.exception.DejaPresentException;
import com.kfokam48.presence.exception.SessionClotureeException;
import com.kfokam48.presence.repository.PresenceRepository;
import com.kfokam48.presence.repository.SessionRepository;
import com.kfokam48.presence.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;

    // Anti-bruteforce : compteur d'échecs par (sessionId, etudiantId)
    private final ConcurrentHashMap<String, Integer> echecsCounter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> bloqueJusqua = new ConcurrentHashMap<>();

    public PresenceResponse marquerPresence(PresenceRequest request) {
        Session session = sessionRepository.findByCode(request.getCode())
                .orElseThrow(CodeInconnuException::new);

        if (session.isCloturee()) {
            throw new SessionClotureeException();
        }

        if (session.isExpiree()) {
            throw new CodeExpireException();
        }

        // Anti-bruteforce : vérifier si bloqué
        String cleBloque = session.getId() + ":" + request.getEtudiantId();
        LocalDateTime bloqueJusquaTime = bloqueJusqua.get(cleBloque);
        if (bloqueJusquaTime != null && LocalDateTime.now().isBefore(bloqueJusquaTime)) {
            throw new RuntimeException("Trop de tentatives, réessayez dans 2 minutes");
        }

        // Vérifier unicité
        if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), request.getEtudiantId())) {
            incrementerEchecs(cleBloque);
            throw new DejaPresentException();
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable"));

        Presence presence = Presence.builder()
                .session(session)
                .etudiant(etudiant)
                .dateHeure(LocalDateTime.now())
                .source(Presence.SourcePresence.ETUDIANT)
                .build();

        presence = presenceRepository.save(presence);
        echecsCounter.remove(cleBloque);
        bloqueJusqua.remove(cleBloque);

        return toResponse(presence);
    }

    public PresenceResponse ajouterPresenceManuelle(PresenceManuelleRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable"));

        if (session.isCloturee()) {
            throw new SessionClotureeException();
        }

        if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), request.getEtudiantId())) {
            throw new DejaPresentException();
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable"));

        Presence presence = Presence.builder()
                .session(session)
                .etudiant(etudiant)
                .dateHeure(LocalDateTime.now())
                .source(Presence.SourcePresence.FORMATEUR)
                .build();

        presence = presenceRepository.save(presence);
        return toResponse(presence);
    }

    public List<Presence> getPresencesBySession(Long sessionId) {
        return presenceRepository.findBySessionId(sessionId);
    }

    public long compterPresencesParSession(Long sessionId) {
        return presenceRepository.countBySessionId(sessionId);
    }

    private void incrementerEchecs(String cle) {
        int count = echecsCounter.merge(cle, 1, Integer::sum);
        if (count >= 5) {
            bloqueJusqua.put(cle, LocalDateTime.now().plusMinutes(2));
            echecsCounter.remove(cle);
        }
    }

    private PresenceResponse toResponse(Presence presence) {
        return PresenceResponse.builder()
                .id(presence.getId())
                .sessionId(presence.getSession().getId())
                .etudiantId(presence.getEtudiant().getId())
                .source(presence.getSource().name())
                .build();
    }
}