package org.example.escapehubbackend.controller;


import org.example.escapehubbackend.dto.PuzzleAttemptRequest;
import org.example.escapehubbackend.dto.PuzzleAttemptResponse;
import org.example.escapehubbackend.engine.MissionRuleEngine;
import org.example.escapehubbackend.service.MissionGameplayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class MissionGameplayController {

    private final MissionGameplayService gameplayService;

    public MissionGameplayController(MissionGameplayService gameplayService) {

        this.gameplayService = gameplayService;

    }


    @PostMapping("/{sessionId}/puzzles/{puzzleId}/attempt")
    public ResponseEntity<?> submitPuzzleAttempt(
            @PathVariable Long sessionId,
            @PathVariable Long puzzleId,
            @RequestBody(required = false) PuzzleAttemptRequest request
    ) {
        try {
            PuzzleAttemptResponse response = gameplayService.submitAttempt(sessionId, puzzleId, request);
            return ResponseEntity.ok(response);
        } catch (MissionRuleEngine.PuzzleLockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", e.getMessage()));
        }
    }






    }






