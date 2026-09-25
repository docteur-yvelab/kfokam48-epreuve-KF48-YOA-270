package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.ExerciceRequest;
import com.kfokam48.presence.dto.ExerciceResponse;
import com.kfokam48.presence.service.ExerciceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/lien")
    public ResponseEntity<ExerciceResponse> remplacerLien(@PathVariable Long id, @RequestBody String nouveauLien) {
        // Accepter le JSON brut pour le nouveau lien
        String lien = nouveauLien.replaceAll("^\"|\"$", "").replaceAll("\\\\", "");
        ExerciceResponse response = exerciceService.remplacerLien(id, lien);
        return ResponseEntity.ok(response);
    }
}