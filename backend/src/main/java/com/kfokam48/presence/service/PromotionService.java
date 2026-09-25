package com.kfokam48.presence.service;

import com.kfokam48.presence.dto.EtudiantResponse;
import com.kfokam48.presence.exception.PromotionInconnueException;
import com.kfokam48.presence.repository.EtudiantRepository;
import com.kfokam48.presence.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    /** GET /api/promotions/{id}/etudiants (contrat) — liste pour la sélection par nom. */
    public List<EtudiantResponse> listerEtudiants(Long promotionId) {
        promotionRepository.findById(promotionId).orElseThrow(PromotionInconnueException::new);
        return etudiantRepository.findByPromotionId(promotionId).stream()
                .map(etudiant -> EtudiantResponse.builder()
                        .id(etudiant.getId())
                        .nom(etudiant.getNom())
                        .prenom(etudiant.getPrenom())
                        .build())
                .toList();
    }
}
