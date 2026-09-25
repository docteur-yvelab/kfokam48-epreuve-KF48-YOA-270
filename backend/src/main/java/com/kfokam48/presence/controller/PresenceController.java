package com.kfokam48.presence.controller;

import com.kfokam48.presence.dto.PresenceRequest;
import com.kfokam48.presence.dto.PresenceResponse;
import com.kfokam48.presence.dto.PresenceManuelleRequest;
import com.kfokam48.presence.service.PresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presences")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @PostMapping
    public ResponseEntity<PresenceResponse> marquerPresence(@Valid @RequestBody PresenceRequest request) {
        PresenceResponse response = presenceService.marquerPresence(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/manuelle")
    public ResponseEntity<PresenceResponse> ajouterPresenceManuelle(@Valid @RequestBody PresenceManuelleRequest request) {
        PresenceResponse response = presenceService.ajouterPresenceManuelle(request);
        return ResponseEntity.status(201).body(response);
    }
}