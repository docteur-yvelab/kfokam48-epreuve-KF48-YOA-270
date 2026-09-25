package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.ExerciceDetailResponse;
import com.kfokam48.presence.dto.ExerciceLienResponse;
import com.kfokam48.presence.dto.ExerciceRequest;
import com.kfokam48.presence.dto.ExerciceResponse;
import com.kfokam48.presence.dto.LienRequest;
import com.kfokam48.presence.service.ExerciceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercices")
@RequiredArgsConstructor
public class ExerciceController {

    private final ExerciceService exerciceService;

    @PostMapping
    public ResponseEntity<ExerciceResponse> deposerExercice(@Valid @RequestBody ExerciceRequest request) {
        ExerciceResponse response = exerciceService.deposerExercice(request);
        return ResponseEntity.status(201).body(response);
    }

    /** GET /api/exercices?sessionId=&etudiantId= (contrat) — 0 ou 1 exercice. */
    @GetMapping
    public ResponseEntity<List<ExerciceDetailResponse>> listerExercices(@RequestParam Long sessionId,
                                                                       @RequestParam Long etudiantId) {
        return ResponseEntity.ok(exerciceService.listerParEtudiant(sessionId, etudiantId));
    }

    /** GET /api/exercices/{id} (contrat) — détail utilisé par l'écran de relecture. */
    @GetMapping("/{id}")
    public ResponseEntity<ExerciceDetailResponse> getExercice(@PathVariable Long id) {
        return ResponseEntity.ok(exerciceService.getExerciceDetail(id));
    }

    /** PUT /api/exercices/{id} (contrat) — corps { "lien": "https://..." }. */
    @PutMapping("/{id}")
    public ResponseEntity<ExerciceLienResponse> remplacerLienPut(@PathVariable Long id,
                                                                @Valid @RequestBody LienRequest request) {
        return ResponseEntity.ok(exerciceService.remplacerLien(id, request.getLien()));
    }

    /** POST /api/exercices/{id}/lien — alias rétro-compatible (corps = lien brut). */
    @PostMapping("/{id}/lien")
    public ResponseEntity<ExerciceLienResponse> remplacerLien(@PathVariable Long id, @RequestBody String nouveauLien) {
        // Accepter le JSON brut pour le nouveau lien
        String lien = nouveauLien.replaceAll("^\"|\"$", "").replaceAll("\\\\", "");
        return ResponseEntity.ok(exerciceService.remplacerLien(id, lien));
    }
}