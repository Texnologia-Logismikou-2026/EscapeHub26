package org.example.escapehubbackend.dto;

public class JoinTeamRequest {
    private Long userId;
    private String teamName;

    public JoinTeamRequest() {}

    public JoinTeamRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
}