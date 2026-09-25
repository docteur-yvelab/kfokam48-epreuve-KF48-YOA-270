package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.ClotureResponse;
import com.kfokam48.presence.dto.SessionDetailResponse;
import com.kfokam48.presence.dto.SessionRequest;
import com.kfokam48.presence.dto.SessionResponse;
import com.kfokam48.presence.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> ouvrirSession(@Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.ouvrirSession(request);
        return ResponseEntity.status(201).body(response);
    }

    /** GET /api/sessions?promotionId= — liste des sessions d'une promotion (contrat). */
    @GetMapping
    public ResponseEntity<List<SessionDetailResponse>> listerSessions(@RequestParam Long promotionId) {
        return ResponseEntity.ok(sessionService.listerSessionsPromotion(promotionId));
    }

    /** GET /api/sessions/{id} — détail d'une session (contrat). */
    @GetMapping("/{id}")
    public ResponseEntity<SessionDetailResponse> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    /** PATCH /api/sessions/{id}/cloture — verbe du contrat, utilisé par le frontend. */
    @PatchMapping("/{id}/cloture")
    public ResponseEntity<ClotureResponse> cloturerSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.cloturerSession(id));
    }

    /** POST /api/sessions/{id}/cloture — alias rétro-compatible. */
    @PostMapping("/{id}/cloture")
    public ResponseEntity<ClotureResponse> cloturerSessionAlias(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.cloturerSession(id));
    }
}