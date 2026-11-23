package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;
import java.util.Arrays;
import java.util.List;

public class SequentialPuzzleValidator implements PuzzleValidator {

    @Override
    public PuzzleType supportedType() {
        return PuzzleType.SEQUENTIAL;
    }

    @Override
    public boolean validate(Puzzle puzzle, MissionSession session, PuzzleAttemptContext context) {
        if (context == null || puzzle.getExpectedAnswer() == null) {
            return false;
        }
        List<String> expectedSteps = Arrays.stream(puzzle.getExpectedAnswer().split("->"))
                .map(String::trim)
                .toList();

        if (context.actionSequence() != null && !context.actionSequence().isEmpty()) {
            List<String> actualSteps = context.actionSequence().stream()
                    .map(String::trim)
                    .toList();
            return expectedSteps.equals(actualSteps);
        }

        if (context.answer() != null) {
            List<String> actualFromAnswer = Arrays.stream(context.answer().split("->"))
                    .map(String::trim)
                    .toList();
            return expectedSteps.equals(actualFromAnswer);
        }

        return false;
    }
}