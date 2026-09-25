package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.RelectureRequest;
import com.kfokam48.presence.dto.RelectureResponse;
import com.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relectures")
@RequiredArgsConstructor
public class RelectureController {

    private final RelectureService relectureService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> soumettreRelecture(
            @PathVariable Long id,
            @Valid @RequestBody RelectureRequest request,
            @RequestHeader("X-Etudiant-Id") Long relecteurId) {
        relectureService.soumettreRelecture(id, request, relecteurId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exercice/{exerciceId}")
    public ResponseEntity<RelectureResponse> getRelectureByExercice(@PathVariable Long exerciceId) {
        RelectureResponse response = relectureService.getRelectureByExercice(exerciceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mes-relectures")
    public ResponseEntity<java.util.List<RelectureResponse>> getMesRelectures(
            @RequestHeader("X-Etudiant-Id") Long relecteurId) {
        var response = relectureService.getRelecturesByRelecteur(relecteurId);
        return ResponseEntity.ok(response);
    }
}