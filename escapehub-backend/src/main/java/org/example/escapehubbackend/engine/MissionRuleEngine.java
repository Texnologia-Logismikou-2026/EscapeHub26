package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MissionRuleEngine {

    private final Map<PuzzleType, PuzzleValidator> validators = new EnumMap<>(PuzzleType.class);

    public MissionRuleEngine() {
        registerValidator(new ExternalApiPuzzleValidator());
        registerValidator(new SequentialPuzzleValidator());
        registerValidator(new CollaborativePuzzleValidator());
    }

    public MissionRuleEngine(List<PuzzleValidator> customValidators) {
        for (PuzzleValidator validator : customValidators) {
            registerValidator(validator);
        }
    }

    private void registerValidator(PuzzleValidator validator) {
        validators.put(validator.supportedType(), validator);
    }

    public boolean isPuzzleUnlocked(MissionSession session, Puzzle puzzle) {
        if (session.getUnlockedPuzzleIds().contains(puzzle.getId())) {
            return true;
        }
        if (puzzle.getRequiredClues() == null || puzzle.getRequiredClues().isEmpty()) {
            session.getUnlockedPuzzleIds().add(puzzle.getId());
            return true;
        }
        if (session.getUnlockedEvidence().containsAll(puzzle.getRequiredClues())) {
            session.getUnlockedPuzzleIds().add(puzzle.getId());
            return true;
        }
        return false;
    }

    public List<Puzzle> evaluateUnlocks(MissionSession session, List<Puzzle> allMissionPuzzles) {
        List<Puzzle> newlyUnlocked = new ArrayList<>();
        for (Puzzle puzzle : allMissionPuzzles) {
            boolean wasAlreadyUnlocked = session.getUnlockedPuzzleIds().contains(puzzle.getId());
            if (!wasAlreadyUnlocked && isPuzzleUnlocked(session, puzzle)) {
                newlyUnlocked.add(puzzle);
            }
        }
        return newlyUnlocked;
    }

    public AttemptOutcome evaluateAttempt(
            MissionSession session,
            Puzzle targetPuzzle,
            List<Puzzle> allMissionPuzzles,
            PuzzleAttemptContext context,
            LocalDateTime timestamp
    ) {
        if (!isPuzzleUnlocked(session, targetPuzzle)) {
            throw new PuzzleLockedException("Puzzle " + targetPuzzle.getCode() + " is locked. Bypass attempt rejected.");
        }

        if (session.getSolvedPuzzles().contains(targetPuzzle.getCode())) {
            return new AttemptOutcome(true, true, null, List.of(), session.isCompleted());
        }

        PuzzleValidator validator = validators.get(targetPuzzle.getType());
        if (validator == null) {
            throw new IllegalStateException("No validator registered for type: " + targetPuzzle.getType());
        }

        boolean solved = validator.validate(targetPuzzle, session, context);
        if (!solved) {
            return new AttemptOutcome(false, false, null, List.of(), session.isCompleted());
        }

        session.getSolvedPuzzles().add(targetPuzzle.getCode());
        if (targetPuzzle.getClueReward() != null && !session.getUnlockedEvidence().contains(targetPuzzle.getClueReward())) {
            session.getUnlockedEvidence().add(targetPuzzle.getClueReward());
        }

        List<Puzzle> newlyUnlocked = evaluateUnlocks(session, allMissionPuzzles);

        if (targetPuzzle.isFinalStage()) {
            session.completeSession(timestamp != null ? timestamp : LocalDateTime.now());
        }

        return new AttemptOutcome(true, false, targetPuzzle.getClueReward(), newlyUnlocked, session.isCompleted());
    }

    public record AttemptOutcome(
            boolean solved,
            boolean alreadySolved,
            String revealedClue,
            List<Puzzle> newlyUnlockedPuzzles,
            boolean missionCompleted
    ) {
    }

    public static class PuzzleLockedException extends RuntimeException {
        public PuzzleLockedException(String message) {
            super(message);
        }
    }
}