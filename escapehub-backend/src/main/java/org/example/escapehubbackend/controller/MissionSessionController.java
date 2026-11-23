package org.example.escapehubbackend.controller;

import org.example.escapehubbackend.dto.MissionSessionResponse;
import org.example.escapehubbackend.dto.StartSessionRequest;
import org.example.escapehubbackend.service.MissionSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class MissionSessionController {

    private final MissionSessionService missionSessionService;

    public MissionSessionController(MissionSessionService missionSessionService) {
        this.missionSessionService = missionSessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<MissionSessionResponse> startSession(@RequestBody StartSessionRequest request) {
        MissionSessionResponse response = missionSessionService.startOrResumeSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<MissionSessionResponse> getSession(@PathVariable Long sessionId) {
        MissionSessionResponse response = missionSessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }
}