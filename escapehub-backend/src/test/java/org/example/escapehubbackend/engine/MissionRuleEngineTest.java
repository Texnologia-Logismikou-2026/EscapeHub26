package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.engine.MissionRuleEngine.AttemptOutcome;
import org.example.escapehubbackend.engine.MissionRuleEngine.PuzzleLockedException;
import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MissionRuleEngineTest {

    private MissionRuleEngine engine;
    private Puzzle pathA;
    private Puzzle pathB;
    private Puzzle pathC;
    private Puzzle finalStage;
    private List<Puzzle> allPuzzles;

    @BeforeEach
    void setUp() {
        engine = new MissionRuleEngine();

        pathA = new Puzzle(1L, "PATH_A", PuzzleType.EXTERNAL_API, "API_42", "CLUE_A", false);
        pathA.setId(101L);

        pathB = new Puzzle(1L, "PATH_B", PuzzleType.SEQUENTIAL, "NORTH->EAST->OPEN", "CLUE_B", false);
        pathB.setId(102L);

        pathC = new Puzzle(1L, "PATH_C", PuzzleType.COLLABORATIVE, "SYNC_KEY", "CLUE_C", false);
        pathC.setId(103L);

        finalStage = new Puzzle(1L, "FINAL_STAGE", PuzzleType.SEQUENTIAL, "A->B->C", "ESCAPE_VICTORY", true);
        finalStage.setId(104L);
        finalStage.setRequiredClues(Set.of("CLUE_A", "CLUE_B", "CLUE_C"));

        allPuzzles = List.of(pathA, pathB, pathC, finalStage);
    }

    @Test
    void nonLinearResolution_OrderABC_UnlocksFinalAndCompletesMission() {
        MissionSession session = new MissionSession(1L, 10L);
        LocalDateTime start = LocalDateTime.of(2026, 11, 21, 20, 0, 0);
        session.setStartedAt(start);

        // Αρχικά οι 3 διαδρομές είναι ξεκλειδωμένες, αλλά το τελικό στάδιο είναι κλειδωμένο
        assertTrue(engine.isPuzzleUnlocked(session, pathA));
        assertTrue(engine.isPuzzleUnlocked(session, pathB));
        assertTrue(engine.isPuzzleUnlocked(session, pathC));
        assertFalse(engine.isPuzzleUnlocked(session, finalStage));

        // 1. Λύση PATH_A (EXTERNAL_API μέσω snapshot)
        AttemptOutcome outA = engine.evaluateAttempt(
                session, pathA, allPuzzles,
                new PuzzleAttemptContext(1L, null, null, Map.of("PATH_A", "api_42")),
                start.plusMinutes(5)
        );
        assertTrue(outA.solved());
        assertFalse(outA.alreadySolved());
        assertEquals("CLUE_A", outA.revealedClue());
        assertTrue(outA.newlyUnlockedPuzzles().isEmpty());
        assertFalse(outA.missionCompleted());

        // 2. Λύση PATH_B (SEQUENTIAL μέσω λίστας ενεργειών)
        AttemptOutcome outB = engine.evaluateAttempt(
                session, pathB, allPuzzles,
                new PuzzleAttemptContext(1L, null, List.of("NORTH", "EAST", "OPEN"), null),
                start.plusMinutes(10)
        );
        assertTrue(outB.solved());
        assertEquals("CLUE_B", outB.revealedClue());
        assertTrue(outB.newlyUnlockedPuzzles().isEmpty());

        // 3. Λύση PATH_C (COLLABORATIVE: 1ος παίκτης δεν αρκεί, 2ος παίκτης ξεκλειδώνει το FINAL_STAGE)
        AttemptOutcome firstPlayer = engine.evaluateAttempt(
                session, pathC, allPuzzles,
                new PuzzleAttemptContext(1L, "SYNC_KEY", null, null),
                start.plusMinutes(12)
        );
        assertFalse(firstPlayer.solved());
        assertFalse(engine.isPuzzleUnlocked(session, finalStage));

        // Ο ίδιος παίκτης ξανά δεν μετράει ως 2ος
        AttemptOutcome samePlayerAgain = engine.evaluateAttempt(
                session, pathC, allPuzzles,
                new PuzzleAttemptContext(1L, "SYNC_KEY", null, null),
                start.plusMinutes(13)
        );
        assertFalse(samePlayerAgain.solved());

        // 2ος διαφορετικός παίκτης λύνει τον γρίφο και ξεκλειδώνει το τελικό στάδιο
        AttemptOutcome secondPlayer = engine.evaluateAttempt(
                session, pathC, allPuzzles,
                new PuzzleAttemptContext(2L, "SYNC_KEY", null, null),
                start.plusMinutes(15)
        );
        assertTrue(secondPlayer.solved());
        assertEquals("CLUE_C", secondPlayer.revealedClue());
        assertEquals(1, secondPlayer.newlyUnlockedPuzzles().size());
        assertEquals("FINAL_STAGE", secondPlayer.newlyUnlockedPuzzles().get(0).getCode());
        assertTrue(engine.isPuzzleUnlocked(session, finalStage));

        // 4. Λύση FINAL_STAGE και ολοκλήρωση αποστολής με καταγραφή χρόνου
        AttemptOutcome finalOut = engine.evaluateAttempt(
                session, finalStage, allPuzzles,
                new PuzzleAttemptContext(1L, "A->B->C", null, null),
                start.plusMinutes(20)
        );
        assertTrue(finalOut.solved());
        assertTrue(finalOut.missionCompleted());
        assertTrue(session.isCompleted());
        assertEquals(1200L, session.getDurationSeconds());
    }

    @Test
    void nonLinearResolution_ReverseOrderCBA_UnlocksSameFinalStage() {
        MissionSession session = new MissionSession(1L, 10L);

        // Σειρά Γ -> Β -> Α
        engine.evaluateAttempt(session, pathC, allPuzzles, new PuzzleAttemptContext(1L, "SYNC_KEY", null, null), LocalDateTime.now());
        AttemptOutcome outC = engine.evaluateAttempt(session, pathC, allPuzzles, new PuzzleAttemptContext(2L, "SYNC_KEY", null, null), LocalDateTime.now());
        assertTrue(outC.solved());
        assertFalse(engine.isPuzzleUnlocked(session, finalStage));

        AttemptOutcome outB = engine.evaluateAttempt(session, pathB, allPuzzles, new PuzzleAttemptContext(1L, "NORTH->EAST->OPEN", null, null), LocalDateTime.now());
        assertTrue(outB.solved());
        assertFalse(engine.isPuzzleUnlocked(session, finalStage));

        AttemptOutcome outA = engine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "API_42", null, null), LocalDateTime.now());
        assertTrue(outA.solved());
        assertTrue(engine.isPuzzleUnlocked(session, finalStage));
        assertEquals(List.of(finalStage), outA.newlyUnlockedPuzzles());
    }

    @Test
    void bypassAttemptOnLockedFinalStage_ThrowsPuzzleLockedException() {
        MissionSession session = new MissionSession(1L, 10L);

        // Λύνουμε μόνο τη διαδρομή Α (λείπουν Β και Γ)
        engine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "API_42", null, null), LocalDateTime.now());

        PuzzleAttemptContext bypassContext = new PuzzleAttemptContext(1L, "A->B->C", null, null);
        assertThrows(PuzzleLockedException.class, () ->
                engine.evaluateAttempt(session, finalStage, allPuzzles, bypassContext, LocalDateTime.now())
        );
        assertFalse(session.isCompleted());
    }

    @Test
    void alreadySolvedPuzzle_ReturnsAlreadySolvedWithoutReawardingClue() {
        MissionSession session = new MissionSession(1L, 10L);

        engine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "API_42", null, null), LocalDateTime.now());
        AttemptOutcome repeat = engine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "API_42", null, null), LocalDateTime.now());

        assertTrue(repeat.solved());
        assertTrue(repeat.alreadySolved());
        assertNull(repeat.revealedClue());
    }

    @Test
    void invalidInputsAndWrongAnswers_ReturnFalse() {
        MissionSession session = new MissionSession(1L, 10L);

        // Null context
        assertFalse(engine.evaluateAttempt(session, pathA, allPuzzles, null, LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathB, allPuzzles, null, LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathC, allPuzzles, null, LocalDateTime.now()).solved());

        // Wrong answers
        assertFalse(engine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "WRONG", null, null), LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathB, allPuzzles, new PuzzleAttemptContext(1L, "NORTH->WRONG", null, null), LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathB, allPuzzles, new PuzzleAttemptContext(1L, null, List.of(), null), LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathC, allPuzzles, new PuzzleAttemptContext(null, "SYNC_KEY", null, null), LocalDateTime.now()).solved());
        assertFalse(engine.evaluateAttempt(session, pathC, allPuzzles, new PuzzleAttemptContext(1L, "WRONG_KEY", null, null), LocalDateTime.now()).solved());
    }

    @Test
    void missingValidator_ThrowsIllegalStateException() {
        MissionRuleEngine emptyEngine = new MissionRuleEngine(List.of());
        MissionSession session = new MissionSession(1L, 10L);

        assertThrows(IllegalStateException.class, () ->
                emptyEngine.evaluateAttempt(session, pathA, allPuzzles, new PuzzleAttemptContext(1L, "API_42", null, null), LocalDateTime.now())
        );
    }
}