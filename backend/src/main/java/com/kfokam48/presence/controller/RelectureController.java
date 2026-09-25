package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.RelectureRequest;
import com.kfokam48.presence.dto.RelectureResponse;
import com.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relectures")
@RequiredArgsConstructor
public class RelectureController {

    private final RelectureService relectureService;

    /**
     * Soumet une relecture (ordre 1 ou 2) pour un exercice.
     * Body: { note, commentaire, ordreRelecteur }
     * Header: X-Etudiant-Id
     */
    @PostMapping("/{exerciceId}")
    public ResponseEntity<RelectureResponse> soumettreRelecture(
            @PathVariable Long exerciceId,
            @Valid @RequestBody RelectureRequest request,
            @RequestHeader("X-Etudiant-Id") Long relecteurId) {
        
        Short ordreRelecteur = request.getOrdreRelecteur();
        if (ordreRelecteur == null || (ordreRelecteur != 1 && ordreRelecteur != 2)) {
            throw new IllegalArgumentException("ordreRelecteur doit être 1 ou 2");
        }
        
        RelectureResponse response = relectureService.soumettreRelecture(exerciceId, ordreRelecteur, request, relecteurId);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste les 2 relectures d'un exercice (ordre 1 et 2).
     */
    @GetMapping("/exercice/{exerciceId}")
    public ResponseEntity<List<RelectureResponse>> getRelecturesByExercice(@PathVariable Long exerciceId) {
        List<RelectureResponse> response = relectureService.getRelecturesByExercice(exerciceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste les relectures en attente pour un relecteur (étudiant).
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<RelectureResponse>> getRelecturesEnAttente(
            @RequestHeader("X-Etudiant-Id") Long relecteurId) {
        List<RelectureResponse> response = relectureService.getRelecturesEnAttentePourRelecteur(relecteurId);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste toutes les relectures faites par un étudiant (en tant que relecteur).
     */
    @GetMapping("/mes-relectures")
    public ResponseEntity<List<RelectureResponse>> getMesRelectures(
            @RequestHeader("X-Etudiant-Id") Long relecteurId) {
        List<RelectureResponse> response = relectureService.getRelecturesByRelecteur(relecteurId);
        return ResponseEntity.ok(response);
    }
}