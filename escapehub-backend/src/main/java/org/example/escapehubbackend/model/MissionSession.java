package org.example.escapehubbackend.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_sessions")
public class MissionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Long missionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mission_session_solved_puzzles", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "puzzle")
    private List<String> solvedPuzzles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mission_session_unlocked_evidence", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "evidence")
    private List<String> unlockedEvidence = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public MissionSession() {
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public MissionSession(Long teamId, Long missionId) {
        this.teamId = teamId;
        this.missionId = missionId;
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public Long getCompletionTimeSeconds() {
        if (startedAt == null || completedAt == null) {
            return null;
        }
        return Math.max(0L, Duration.between(startedAt, completedAt).getSeconds());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public List<String> getSolvedPuzzles() { return solvedPuzzles; }
    public void setSolvedPuzzles(List<String> solvedPuzzles) {
        this.solvedPuzzles = solvedPuzzles != null ? solvedPuzzles : new ArrayList<>();
    }

    public List<String> getUnlockedEvidence() { return unlockedEvidence; }
    public void setUnlockedEvidence(List<String> unlockedEvidence) {
        this.unlockedEvidence = unlockedEvidence != null ? unlockedEvidence : new ArrayList<>();
    }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}