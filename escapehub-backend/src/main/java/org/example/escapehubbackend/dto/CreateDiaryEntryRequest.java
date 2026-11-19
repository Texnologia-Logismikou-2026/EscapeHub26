package org.example.escapehubbackend.dto;

public class CreateDiaryEntryRequest {
    private Long teamId;
    private String authorName;
    private String content;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}