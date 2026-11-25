package org.example.escapehubbackend.engine;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;

public interface PuzzleValidator {
    PuzzleType supportedType();
    boolean validate(Puzzle puzzle, MissionSession session, PuzzleAttemptContext context);
}