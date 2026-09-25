package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.SessionRequest;
import com.kfokam48.presence.dto.SessionResponse;
import com.kfokam48.presence.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/cloture")
    public ResponseEntity<Void> cloturerSession(@PathVariable Long id) {
        sessionService.cloturerSession(id);
        return ResponseEntity.ok().build();
    }
}