package org.example.escapehubbackend.dto;

public class StartSessionRequest {
    private Long teamId;
    private Long missionId;

    public StartSessionRequest() {
    }

    public StartSessionRequest(Long teamId, Long missionId) {
        this.teamId = teamId;
        this.missionId = missionId;
    }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
}
