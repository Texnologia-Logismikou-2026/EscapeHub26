package org.example.escapehubbackend.controller;

import org.example.escapehubbackend.dto.PuzzleAttemptRequest;
import org.example.escapehubbackend.dto.PuzzleAttemptResponse;
import org.example.escapehubbackend.engine.MissionRuleEngine.PuzzleLockedException;
import org.example.escapehubbackend.service.MissionGameplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MissionSessionControllerTest {

    private MockMvc mockMvc;
    private MissionGameplayService gameplayService;

    @BeforeEach
    void setUp() {
        gameplayService = Mockito.mock(MissionGameplayService.class);
        MissionGameplayController controller = new MissionGameplayController(gameplayService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void submitAttempt_WhenPuzzleIsLocked_Returns403Forbidden() throws Exception {
        when(gameplayService.submitAttempt(eq(1L), eq(99L), any(PuzzleAttemptRequest.class)))
                .thenThrow(new PuzzleLockedException("Puzzle FINAL_STAGE is locked. Bypass attempt rejected."));

        mockMvc.perform(post("/api/sessions/1/puzzles/99/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "answer": "A->B->C"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void submitAttempt_WhenSolved_Returns200OkWithOutcome() throws Exception {
        PuzzleAttemptResponse response = new PuzzleAttemptResponse(
                true, false, "CLUE_A", List.of("FINAL_STAGE"), false, 45L
        );
        when(gameplayService.submitAttempt(eq(1L), eq(10L), any(PuzzleAttemptRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/sessions/1/puzzles/10/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "answer": "API_42"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }
}