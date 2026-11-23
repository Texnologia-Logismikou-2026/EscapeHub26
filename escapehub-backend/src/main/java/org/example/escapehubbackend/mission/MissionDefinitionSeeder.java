package org.example.escapehubbackend.mission;

import org.example.escapehubbackend.model.Mission;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.PuzzleType;
import org.example.escapehubbackend.repository.MissionRepository;
import org.example.escapehubbackend.repository.PuzzleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionDefinitionSeeder implements ApplicationRunner {

    private final MissionDefinitionLoader definitionLoader;
    private final MissionRepository missionRepository;
    private final PuzzleRepository puzzleRepository;
    private final Environment environment;

    public MissionDefinitionSeeder(
            MissionDefinitionLoader definitionLoader,
            MissionRepository missionRepository,
            PuzzleRepository puzzleRepository,
            Environment environment
    ) {
        this.definitionLoader = definitionLoader;
        this.missionRepository = missionRepository;
        this.puzzleRepository = puzzleRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (environment.acceptsProfiles(Profiles.of("h2"))) {
            return;
        }
        seedAll();
    }

    @Transactional
    public void seedAll() {
        Map<String, Mission> existingMissionsByTitle = missionRepository.findAll().stream()
                .collect(Collectors.toMap(Mission::getTitle, m -> m, (first, second) -> first));

        for (MissionDefinition definition : definitionLoader.findAll()) {
            Mission mission = existingMissionsByTitle.computeIfAbsent(
                    definition.title(),
                    title -> missionRepository.save(new Mission(definition.title(), definition.description()))
            );

            seedPuzzlesForMission(mission.getId(), definition);
        }
    }

    private void seedPuzzlesForMission(Long persistedMissionId, MissionDefinition definition) {
        Set<String> existingCodes = puzzleRepository.findByMissionId(persistedMissionId).stream()
                .map(Puzzle::getCode)
                .collect(Collectors.toSet());

        Map<String, String> rewardByPuzzleId = new HashMap<>();
        for (MissionDefinition.PuzzleDefinition puzzleDef : definition.puzzles()) {
            if (puzzleDef.rewardEvidenceId() != null && !puzzleDef.rewardEvidenceId().isBlank()) {
                rewardByPuzzleId.put(puzzleDef.id(), puzzleDef.rewardEvidenceId());
            }
        }

        String finalPuzzleId = definition.escapeCondition() != null
                ? definition.escapeCondition().finalPuzzleId()
                : null;

        for (MissionDefinition.PuzzleDefinition puzzleDef : definition.puzzles()) {
            if (existingCodes.contains(puzzleDef.id())) {
                continue;
            }

            Puzzle puzzle = new Puzzle(
                    persistedMissionId,
                    puzzleDef.id(),
                    mapPuzzleType(puzzleDef.type()),
                    extractExpectedAnswer(puzzleDef.validationRule()),
                    puzzleDef.rewardEvidenceId(),
                    puzzleDef.id().equals(finalPuzzleId),
                    resolveRequiredClues(puzzleDef, rewardByPuzzleId)
            );
            puzzleRepository.save(puzzle);
        }
    }

    private PuzzleType mapPuzzleType(String rawType) {
        if (rawType == null) {
            return PuzzleType.EXTERNAL_API;
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("COLLABORATIVE")) {
            return PuzzleType.COLLABORATIVE;
        }
        if (normalized.contains("SEQUENTIAL")) {
            return PuzzleType.SEQUENTIAL;
        }
        return PuzzleType.EXTERNAL_API;
    }

    private String extractExpectedAnswer(String validationRule) {
        if (validationRule == null || validationRule.isBlank()) {
            return "";
        }
        int separatorIndex = validationRule.indexOf(':');
        return separatorIndex >= 0
                ? validationRule.substring(separatorIndex + 1).trim()
                : validationRule.trim();
    }

    private List<String> resolveRequiredClues(
            MissionDefinition.PuzzleDefinition puzzleDef,
            Map<String, String> rewardByPuzzleId
    ) {
        if (puzzleDef.unlockRule() == null || puzzleDef.unlockRule().requiredSolvedPuzzles() == null) {
            return new ArrayList<>();
        }
        List<String> requiredClues = new ArrayList<>();
        for (String requiredPuzzleId : puzzleDef.unlockRule().requiredSolvedPuzzles()) {
            String clue = rewardByPuzzleId.get(requiredPuzzleId);
            if (clue != null) {
                requiredClues.add(clue);
            }
        }
        return requiredClues;
    }
}