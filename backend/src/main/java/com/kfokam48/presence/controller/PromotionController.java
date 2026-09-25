package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.EtudiantResponse;
import com.kfokam48.presence.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /** GET /api/promotions/{id}/etudiants (contrat) — utilisé par les écrans Étudiant et Relecteur. */
    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<EtudiantResponse>> listerEtudiants(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.listerEtudiants(id));
    }
}
