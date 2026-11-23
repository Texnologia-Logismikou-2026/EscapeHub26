package org.example.escapehubbackend.mission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionDefinitionLoader {

    public static final String DEFAULT_MISSIONS_PATTERN = "classpath*:missions/*.json";

    private final ObjectMapper objectMapper;
    private final Map<String, MissionDefinition> missionsByCode;
    private final Map<Long, MissionDefinition> missionsById;

    @Autowired
    public MissionDefinitionLoader(ObjectMapper objectMapper) {
        this(objectMapper, DEFAULT_MISSIONS_PATTERN);
    }

    MissionDefinitionLoader(ObjectMapper objectMapper, String resourcePattern) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Map<String, MissionDefinition> byCode = new LinkedHashMap<>();
        Map<Long, MissionDefinition> byId = new LinkedHashMap<>();

        loadAllFromPattern(resourcePattern).forEach(mission -> {
            if (byCode.putIfAbsent(mission.code(), mission) != null) {
                throw new IllegalStateException("Duplicate mission code: " + mission.code());
            }
            if (byId.putIfAbsent(mission.missionId(), mission) != null) {
                throw new IllegalStateException("Duplicate missionId: " + mission.missionId());
            }
        });

        this.missionsByCode = Collections.unmodifiableMap(byCode);
        this.missionsById = Collections.unmodifiableMap(byId);
    }

    public List<MissionDefinition> findAll() {
        return List.copyOf(missionsByCode.values());
    }

    public Optional<MissionDefinition> findByCode(String code) {
        return Optional.ofNullable(missionsByCode.get(code));
    }

    public Optional<MissionDefinition> findByMissionId(Long missionId) {
        return Optional.ofNullable(missionsById.get(missionId));
    }

    public List<String> getDefaultUnlockedEvidenceIds(String code) {
        return findByCode(code)
                .map(mission -> mission.evidence().stream()
                        .filter(MissionDefinition.EvidenceDefinition::unlockedByDefault)
                        .map(MissionDefinition.EvidenceDefinition::id)
                        .toList())
                .orElse(List.of());
    }

    public MissionDefinition loadFromStream(InputStream inputStream) {
        try {
            MissionDefinition definition = objectMapper.readValue(inputStream, MissionDefinition.class);
            validate(definition);
            return definition;
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalArgumentException("Invalid mission definition JSON", e);
        }
    }

    private List<MissionDefinition> loadAllFromPattern(String pattern) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(pattern);
            List<MissionDefinition> loaded = new ArrayList<>();
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    loaded.add(loadFromStream(is));
                } catch (IOException | RuntimeException e) {
                    throw new IllegalStateException(
                            "Failed to load mission file: " + resource.getFilename(), e);
                }
            }
            return loaded;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan mission resources: " + pattern, e);
        }
    }

    private void validate(MissionDefinition mission) {
        if (mission == null) {
            throw new IllegalArgumentException("Mission definition cannot be null");
        }
        if (mission.missionId() == null) {
            throw new IllegalArgumentException("missionId is required");
        }
        if (mission.code() == null || mission.code().isBlank()) {
            throw new IllegalArgumentException("Mission code is required");
        }
        if (mission.title() == null || mission.title().isBlank()) {
            throw new IllegalArgumentException("Mission title is required");
        }
        if (mission.paths() == null || mission.paths().isEmpty()) {
            throw new IllegalArgumentException("Mission must define at least one path");
        }
        if (mission.puzzles() == null || mission.puzzles().isEmpty()) {
            throw new IllegalArgumentException("Mission must define at least one puzzle");
        }
        if (mission.escapeCondition() == null || mission.escapeCondition().finalPuzzleId() == null) {
            throw new IllegalArgumentException("Mission escapeCondition.finalPuzzleId is required");
        }

        Set<String> puzzleIds = mission.puzzles().stream()
                .map(MissionDefinition.PuzzleDefinition::id)
                .collect(Collectors.toSet());

        if (!puzzleIds.contains(mission.escapeCondition().finalPuzzleId())) {
            throw new IllegalArgumentException(
                    "finalPuzzleId does not match any defined puzzle: " + mission.escapeCondition().finalPuzzleId());
        }

        for (MissionDefinition.PuzzleDefinition puzzle : mission.puzzles()) {
            if (puzzle.unlockRule() != null && puzzle.unlockRule().requiredSolvedPuzzles() != null) {
                for (String requiredPuzzle : puzzle.unlockRule().requiredSolvedPuzzles()) {
                    if (!puzzleIds.contains(requiredPuzzle)) {
                        throw new IllegalArgumentException(
                                "Puzzle " + puzzle.id() + " references unknown required puzzle: " + requiredPuzzle);
                    }
                }
            }
        }
    }
}