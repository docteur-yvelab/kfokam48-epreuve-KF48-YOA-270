package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.TableauEtudiantResponse;
import com.kfokam48.presence.service.TableauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tableau")
@RequiredArgsConstructor
public class TableauController {

    private final TableauService tableauService;

    @GetMapping
    public ResponseEntity<List<TableauEtudiantResponse>> getTableau(@RequestParam Long promotionId) {
        List<TableauEtudiantResponse> response = tableauService.getTableau(promotionId);
        return ResponseEntity.ok(response);
    }
}