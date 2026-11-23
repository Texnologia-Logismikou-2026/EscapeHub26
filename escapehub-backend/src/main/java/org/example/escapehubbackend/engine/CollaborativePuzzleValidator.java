package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;

public class CollaborativePuzzleValidator implements PuzzleValidator {

    @Override
    public PuzzleType supportedType() {
        return PuzzleType.COLLABORATIVE;
    }

    @Override
    public boolean validate(Puzzle puzzle, MissionSession session, PuzzleAttemptContext context) {
        if (context == null || context.userId() == null || context.answer() == null || puzzle.getExpectedAnswer() == null) {
            return false;
        }
        if (!context.answer().trim().equalsIgnoreCase(puzzle.getExpectedAnswer().trim())) {
            return false;
        }

        String prefix = puzzle.getId() + ":";
        session.getCollaborativeContributions().add(prefix + context.userId());

        long distinctContributors = session.getCollaborativeContributions().stream()
                .filter(entry -> entry.startsWith(prefix))
                .count();

        return distinctContributors >= 2;
    }
}