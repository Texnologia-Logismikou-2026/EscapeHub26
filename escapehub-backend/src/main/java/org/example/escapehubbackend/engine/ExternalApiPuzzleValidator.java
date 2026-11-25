package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;

public class ExternalApiPuzzleValidator implements PuzzleValidator {

    @Override
    public PuzzleType supportedType() {
        return PuzzleType.EXTERNAL_API;
    }

    @Override
    public boolean validate(Puzzle puzzle, MissionSession session, PuzzleAttemptContext context) {
        if (context == null || puzzle.getExpectedAnswer() == null) {
            return false;
        }
        String expected = puzzle.getExpectedAnswer().trim();
        if (context.externalApiSnapshots() != null && context.externalApiSnapshots().containsKey(puzzle.getCode())) {
            String snapshotValue = context.externalApiSnapshots().get(puzzle.getCode());
            return snapshotValue != null && snapshotValue.trim().equalsIgnoreCase(expected);
        }
        return context.answer() != null && context.answer().trim().equalsIgnoreCase(expected);
    }
}