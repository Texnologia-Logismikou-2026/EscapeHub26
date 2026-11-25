package org.example.escapehubbackend.mission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionDefinitionLoaderTest {

    private MissionDefinitionLoader loader;

    @BeforeEach
    void setUp() {
        loader = new MissionDefinitionLoader(JsonMapper.builder().build());
    }

    @Test
    void loadsEncryptedLibraryMissionWithNonLinearStructure() {
        MissionDefinition mission = loader.findByCode("encrypted-library").orElseThrow();

        assertThat(loader.findByMissionId(1L)).contains(mission);
        assertThat(mission.paths()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(mission.puzzles()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(mission.externalApis()).contains("Open Library", "Wikidata");
        assertThat(mission.escapeCondition().finalPuzzleId()).isEqualTo("puzzle-d");

        MissionDefinition.PuzzleDefinition finalPuzzle = mission.puzzles().stream()
                .filter(p -> p.id().equals("puzzle-d"))
                .findFirst()
                .orElseThrow();
        assertThat(finalPuzzle.unlockRule().operator()).isEqualTo("AND");
        assertThat(finalPuzzle.unlockRule().requiredSolvedPuzzles())
                .containsExactlyInAnyOrder("puzzle-a", "puzzle-b", "puzzle-c");

        assertThat(loader.getDefaultUnlockedEvidenceIds("encrypted-library"))
                .containsExactlyInAnyOrder("evidence-1", "evidence-2", "evidence-3", "evidence-4");
    }

    @Test
    void loadsSecondMissionWithoutAnyCodeChanges() {
        String secondMissionJson = """
                {
                  "missionId": 2,
                  "code": "athens-cipher",
                  "title": "Ο Κώδικας της Αθήνας",
                  "category": "Ιστορία",
                  "description": "Δεύτερη αποστολή για απόδειξη data-driven επεκτασιμότητας.",
                  "minPlayers": 1,
                  "maxPlayers": 3,
                  "externalApis": ["Wikidata"],
                  "paths": [
                    {
                      "id": "path-1",
                      "name": "Διαδρομή 1",
                      "description": "Εισαγωγική έρευνα",
                      "puzzleIds": ["p1", "p2"]
                    }
                  ],
                  "evidence": [
                    {
                      "id": "ev-1",
                      "title": "Χάρτης",
                      "content": "Αρχικό στοιχείο",
                      "pathId": "path-1",
                      "unlockedByDefault": true
                    }
                  ],
                  "puzzles": [
                    {
                      "id": "p1",
                      "pathId": "path-1",
                      "title": "Γρίφος 1",
                      "type": "API_ENTITY_PROPERTY",
                      "prompt": "Ερώτηση 1",
                      "apiSource": null,
                      "requiredEvidenceIds": ["ev-1"],
                      "unlockRule": { "operator": "NONE", "requiredSolvedPuzzles": [] },
                      "rewardEvidenceId": null,
                      "validationRule": "EXACT_NUMBER:42"
                    },
                    {
                      "id": "p2",
                      "pathId": "path-1",
                      "title": "Τελικός Γρίφος",
                      "type": "COMBINED_CORRELATION",
                      "prompt": "Τελική ερώτηση",
                      "apiSource": null,
                      "requiredEvidenceIds": ["ev-1"],
                      "unlockRule": { "operator": "AND", "requiredSolvedPuzzles": ["p1"] },
                      "rewardEvidenceId": null,
                      "validationRule": "CASE_INSENSITIVE_TEXT:ATHENS"
                    }
                  ],
                  "escapeCondition": {
                    "finalPuzzleId": "p2",
                    "description": "Επίλυση του p2"
                  }
                }
                """;

        MissionDefinition secondMission = loader.loadFromStream(
                new ByteArrayInputStream(secondMissionJson.getBytes(StandardCharsets.UTF_8)));

        assertThat(secondMission.missionId()).isEqualTo(2L);
        assertThat(secondMission.code()).isEqualTo("athens-cipher");
        assertThat(secondMission.puzzles()).extracting(MissionDefinition.PuzzleDefinition::id)
                .isEqualTo(List.of("p1", "p2"));
    }

    @Test
    void rejectsMissionWithUnknownFinalPuzzle() {
        String brokenMissionJson = """
                {
                  "missionId": 99,
                  "code": "broken-mission",
                  "title": "Broken",
                  "category": "Test",
                  "description": "Invalid",
                  "minPlayers": 1,
                  "maxPlayers": 2,
                  "externalApis": [],
                  "paths": [{ "id": "p", "name": "P", "description": "D", "puzzleIds": ["q1"] }],
                  "evidence": [],
                  "puzzles": [{
                    "id": "q1",
                    "pathId": "p",
                    "title": "Q1",
                    "type": "TEXT",
                    "prompt": "?",
                    "apiSource": null,
                    "requiredEvidenceIds": [],
                    "unlockRule": { "operator": "NONE", "requiredSolvedPuzzles": [] },
                    "rewardEvidenceId": null,
                    "validationRule": "TEXT:A"
                  }],
                  "escapeCondition": {
                    "finalPuzzleId": "non-existent-puzzle",
                    "description": "Broken"
                  }
                }
                """;

        assertThatThrownBy(() -> loader.loadFromStream(
                new ByteArrayInputStream(brokenMissionJson.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-existent-puzzle");
    }
}