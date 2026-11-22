package org.example.escapehubbackend.engine;

import java.util.List;
import java.util.Map;

public record PuzzleAttemptContext(
        Long userId,
        String answer,
        List<String> actionSequence,
        Map<String,String> externalApiSnapshots
) {

}
