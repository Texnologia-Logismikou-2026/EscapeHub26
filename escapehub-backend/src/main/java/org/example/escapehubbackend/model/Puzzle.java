package org.example.escapehubbackend.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="puzzles")
public class Puzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long missionId;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PuzzleType type;

    @Column(nullable = false)
    private String expectedAnswer;

    private String clueReward;

    private boolean finalStage;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "puzzle_required_clues", joinColumns = @JoinColumn(name = "puzzle_id"))
    @Column(name = "clue")
    private List<String> requiredClues = new ArrayList<>();

    public Puzzle() {}

    public Puzzle(Long missionId, String code, PuzzleType type, String expectedAnswer,
                  String clueReward, boolean finalStage, List<String> requiredClues) {
        this.missionId = missionId;
        this.code = code;
        this.type = type;
        this.expectedAnswer = expectedAnswer;
        this.clueReward = clueReward;
        this.finalStage = finalStage;
        this.requiredClues = requiredClues != null ? requiredClues : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public PuzzleType getType() { return type; }
    public void setType(PuzzleType type) { this.type = type; }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }

    public String getClueReward() { return clueReward; }
    public void setClueReward(String clueReward) { this.clueReward = clueReward; }

    public boolean isFinalStage() { return finalStage; }
    public void setFinalStage(boolean finalStage) { this.finalStage = finalStage; }

    public List<String> getRequiredClues() { return requiredClues; }
    public void setRequiredClues(List<String> requiredClues) {
        this.requiredClues = requiredClues != null ? requiredClues : new ArrayList<>();
    }

    public Puzzle(Long missionId, String code, PuzzleType type, String expectedAnswer,
                  String clueReward, boolean finalStage) {
        this(missionId, code, type, expectedAnswer, clueReward, finalStage, new ArrayList<>());
    }

    public void setRequiredClues(java.util.Collection<String> requiredClues) {
        this.requiredClues = requiredClues != null ? new ArrayList<>(requiredClues) : new ArrayList<>();
    }

}
