package org.example.escapehubbackend.dto;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class MissionSessionResponse {
    private Long id;
    private Long teamId;
    private Long missionId;
    private SessionStatus status;
    private List<String> solvedPuzzles;
    private List<String> unlockedEvidence;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long completionTimeSeconds;

    public MissionSessionResponse() {
    }

    public static MissionSessionResponse fromEntity(MissionSession session) {
        MissionSessionResponse dto = new MissionSessionResponse();
        dto.setId(session.getId());
        dto.setTeamId(session.getTeamId());
        dto.setMissionId(session.getMissionId());
        dto.setStatus(session.getStatus());
        dto.setSolvedPuzzles(session.getSolvedPuzzles());
        dto.setUnlockedEvidence(session.getUnlockedEvidence());
        dto.setStartedAt(session.getStartedAt());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setCompletionTimeSeconds(session.getCompletionTimeSeconds());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public List<String> getSolvedPuzzles() {
        return solvedPuzzles;
    }

    public void setSolvedPuzzles(List<String> solvedPuzzles) {
        this.solvedPuzzles = solvedPuzzles;
    }

    public List<String> getUnlockedEvidence() {
        return unlockedEvidence;
    }

    public void setUnlockedEvidence(List<String> unlockedEvidence) {
        this.unlockedEvidence = unlockedEvidence;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getCompletionTimeSeconds() {
        return completionTimeSeconds;
    }

    public void setCompletionTimeSeconds(Long completionTimeSeconds) {
        this.completionTimeSeconds = completionTimeSeconds;
    }
}