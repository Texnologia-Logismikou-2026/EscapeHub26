package org.example.escapehubbackend.controller;

import org.example.escapehubbackend.mission.MissionDefinition;
import org.example.escapehubbackend.mission.MissionDefinitionLoader;
import org.example.escapehubbackend.model.Mission;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.repository.MissionRepository;
import org.example.escapehubbackend.repository.PuzzleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DemoWiringController {

    private final MissionRepository missionRepository;
    private final PuzzleRepository puzzleRepository;
    private final MissionDefinitionLoader definitionLoader;

    public DemoWiringController(
            MissionRepository missionRepository,
            PuzzleRepository puzzleRepository,
            MissionDefinitionLoader definitionLoader
    ) {
        this.missionRepository = missionRepository;
        this.puzzleRepository = puzzleRepository;
        this.definitionLoader = definitionLoader;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
                "message", "Backend connected (Spring Boot + PostgreSQL)",
                "version", "1.0.0",
                "branch", "demo-wiring"
        );
    }

    @GetMapping("/search")
    public Map<String, String> search(@RequestParam String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return Map.of("message", "Please enter a search term.");
        }

        List<String> hits = new ArrayList<>();

        for (Mission m : missionRepository.findAll()) {
            if ((m.getTitle() != null && m.getTitle().toLowerCase(Locale.ROOT).contains(q))
                    || (m.getDescription() != null && m.getDescription().toLowerCase(Locale.ROOT).contains(q))) {
                hits.add("Mission: " + m.getTitle());
            }
        }

        for (MissionDefinition def : definitionLoader.findAll()) {
            if (def.evidence() != null) {
                for (MissionDefinition.EvidenceDefinition ev : def.evidence()) {
                    if (ev.unlockedByDefault() && (
                            (ev.title() != null && ev.title().toLowerCase(Locale.ROOT).contains(q))
                                    || (ev.content() != null && ev.content().toLowerCase(Locale.ROOT).contains(q)))) {
                        hits.add(ev.title() + " — " + ev.content());
                    }
                }
            }
        }

        if (hits.isEmpty()) {
            return Map.of("message", "No results found for \"" + query + "\".");
        }
        return Map.of("message", String.join(" | ", hits));
    }

    @GetMapping("/missions/{missionId}/puzzles")
    public ResponseEntity<?> getMissionPuzzles(@PathVariable Long missionId) {
        Optional<Mission> missionOpt = missionRepository.findById(missionId);
        if (missionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Mission mission = missionOpt.get();

        MissionDefinition definition = definitionLoader.findAll().stream()
                .filter(d -> d.title() != null && d.title().equals(mission.getTitle()))
                .findFirst()
                .orElse(null);

        Map<String, MissionDefinition.PuzzleDefinition> defByCode = new HashMap<>();
        Map<String, MissionDefinition.EvidenceDefinition> evidenceById = new HashMap<>();

        if (definition != null) {
            if (definition.puzzles() != null) {
                for (MissionDefinition.PuzzleDefinition p : definition.puzzles()) {
                    defByCode.put(p.id(), p);
                }
            }
            if (definition.evidence() != null) {
                for (MissionDefinition.EvidenceDefinition ev : definition.evidence()) {
                    evidenceById.put(ev.id(), ev);
                }
            }
        }

        Map<String, String> evidenceMap = evidenceById.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().title() + ": " + e.getValue().content()
                ));

        List<Puzzle> puzzles = puzzleRepository.findByMissionId(missionId).stream()
                .sorted(Comparator.comparing(Puzzle::getId))
                .toList();

        List<Object> result = new ArrayList<>();
        for (Puzzle puzzle : puzzles) {
            MissionDefinition.PuzzleDefinition pDef = defByCode.get(puzzle.getCode());

            List<String> clues = new ArrayList<>();
            if (pDef != null && pDef.requiredEvidenceIds() != null) {
                for (String evId : pDef.requiredEvidenceIds()) {
                    MissionDefinition.EvidenceDefinition ev = evidenceById.get(evId);
                    if (ev != null && ev.unlockedByDefault()) {
                        clues.add(ev.title() + ": " + ev.content());
                    }
                }
            }
            if (clues.isEmpty()) {
                clues.add("Check the mission clues and unlocked evidence in your team diary.");
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", puzzle.getId());
            item.put("code", puzzle.getCode());
            item.put("type", puzzle.getType().name());
            item.put("title", pDef != null && pDef.title() != null ? pDef.title() : puzzle.getCode());
            item.put("prompt", pDef != null && pDef.prompt() != null ? pDef.prompt() : "Enter the answer for " + puzzle.getCode());
            item.put("clues", clues);
            item.put("requiredClues", puzzle.getRequiredClues() != null ? puzzle.getRequiredClues() : List.of());
            item.put("clueReward", puzzle.getClueReward());
            item.put("finalStage", puzzle.isFinalStage());
            item.put("evidenceMap", evidenceMap);
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }
}