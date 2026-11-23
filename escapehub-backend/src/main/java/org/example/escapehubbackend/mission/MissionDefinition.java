package org.example.escapehubbackend.mission;

import java.util.List;

public record MissionDefinition(
        Long missionId,
        String code,
        String title,
        String category,
        String description,
        int minPlayers,
        int maxPlayers,
        List<String> externalApis,
        List<PathDefinition> paths,
        List<EvidenceDefinition> evidence,
        List<PuzzleDefinition> puzzles,
        EscapeCondition escapeCondition
) {
    public record PathDefinition(
            String id,
            String name,
            String description,
            List<String> puzzleIds
    ) {}

    public record EvidenceDefinition(
            String id,
            String title,
            String content,
            String pathId,
            boolean unlockedByDefault
    ) {}

    public record PuzzleDefinition(
            String id,
            String pathId,
            String title,
            String type,
            String prompt,
            ApiSourceSpec apiSource,
            List<String> requiredEvidenceIds,
            UnlockRule unlockRule,
            String rewardEvidenceId,
            String validationRule
    ) {}

    public record ApiSourceSpec(
            String provider,
            String endpoint,
            String targetField,
            String snapshotKey
    ) {}

    public record UnlockRule(
            String operator,
            List<String> requiredSolvedPuzzles
    ) {}

    public record EscapeCondition(
            String finalPuzzleId,
            String description
    ) {}
}