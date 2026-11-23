package org.example.escapehubbackend.mission;

import org.example.escapehubbackend.engine.MissionRuleEngine;
import org.example.escapehubbackend.engine.PuzzleAttemptContext;
import org.example.escapehubbackend.model.Mission;
import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.repository.MissionRepository;
import org.example.escapehubbackend.repository.PuzzleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class MissionDefinitionSeederTest {

    @Autowired
    private MissionDefinitionLoader definitionLoader;

    @Autowired
    private MissionDefinitionSeeder seeder;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private PuzzleRepository puzzleRepository;

    @Test
    void seedsEncryptedLibraryMissionAndPuzzlesIdempotently() {
        String expectedTitle = definitionLoader.findByCode("encrypted-library")
                .orElseThrow()
                .title();

        seeder.seedAll();
        seeder.seedAll();

        List<Mission> seededMissions = missionRepository.findAll().stream()
                .filter(m -> expectedTitle.equals(m.getTitle()))
                .toList();

        assertThat(seededMissions).hasSize(1);

        List<Puzzle> puzzles = puzzleRepository.findByMissionId(seededMissions.get(0).getId());
        assertThat(puzzles).extracting(Puzzle::getCode)
                .containsExactlyInAnyOrder("puzzle-a", "puzzle-b", "puzzle-c", "puzzle-d");

        Puzzle finalPuzzle = puzzles.stream()
                .filter(p -> "puzzle-d".equals(p.getCode()))
                .findFirst()
                .orElseThrow();
        assertThat(finalPuzzle.isFinalStage()).isTrue();
        assertThat(finalPuzzle.getRequiredClues())
                .containsExactlyInAnyOrder("evidence-5", "evidence-6", "evidence-7");
    }

    @Test
    void seededMissionRunsEndToEndThroughMissionRuleEngine() {
        String expectedTitle = definitionLoader.findByCode("encrypted-library")
                .orElseThrow()
                .title();

        seeder.seedAll();

        Mission mission = missionRepository.findAll().stream()
                .filter(m -> expectedTitle.equals(m.getTitle()))
                .findFirst()
                .orElseThrow();

        List<Puzzle> allPuzzles = puzzleRepository.findByMissionId(mission.getId());
        Map<String, Puzzle> byCode = allPuzzles.stream()
                .collect(Collectors.toMap(Puzzle::getCode, Function.identity()));

        MissionSession session = new MissionSession(10L, mission.getId());
        MissionRuleEngine ruleEngine = new MissionRuleEngine();
        LocalDateTime now = LocalDateTime.now();

        // 1. Anti-bypass: το τελικό στάδιο είναι κλειδωμένο πριν λυθούν οι 3 διαδρομές
        assertThatThrownBy(() -> ruleEngine.evaluateAttempt(
                session,
                byCode.get("puzzle-d"),
                allPuzzles,
                new PuzzleAttemptContext(1L, "NANTES-73", null, null),
                now
        )).isInstanceOf(MissionRuleEngine.PuzzleLockedException.class);

        // 2. Μη γραμμική επίλυση (C -> A -> B)
        var outcomeC = ruleEngine.evaluateAttempt(
                session, byCode.get("puzzle-c"), allPuzzles,
                new PuzzleAttemptContext(1L, "Nantes", null, null), now);
        assertThat(outcomeC.solved()).isTrue();
        assertThat(outcomeC.revealedClue()).isEqualTo("evidence-7");

        var outcomeA = ruleEngine.evaluateAttempt(
                session, byCode.get("puzzle-a"), allPuzzles,
                new PuzzleAttemptContext(1L, null, null, Map.of("puzzle-a", "1870")), now);
        assertThat(outcomeA.solved()).isTrue();
        assertThat(outcomeA.revealedClue()).isEqualTo("evidence-5");

        var outcomeB = ruleEngine.evaluateAttempt(
                session, byCode.get("puzzle-b"), allPuzzles,
                new PuzzleAttemptContext(2L, "1797", null, null), now);
        assertThat(outcomeB.solved()).isTrue();
        assertThat(outcomeB.newlyUnlockedPuzzles()).extracting(Puzzle::getCode).contains("puzzle-d");

        // 3. Επίλυση τελικού χρηματοκιβωτίου (λειτουργεί και solo και σε ομάδα, case-insensitive)
        var finalOutcome = ruleEngine.evaluateAttempt(
                session, byCode.get("puzzle-d"), allPuzzles,
                new PuzzleAttemptContext(1L, "nantes-73", null, null), now);
        assertThat(finalOutcome.solved()).isTrue();
        assertThat(finalOutcome.missionCompleted()).isTrue();
    }
}