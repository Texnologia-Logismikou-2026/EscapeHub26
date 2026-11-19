package org.example.escapehubbackend.dto;

public class JoinTeamRequest {
    private Long userId;

    public JoinTeamRequest(){

    }

    public JoinTeamRequest(Long userId){

        this.userId=userId;

    }

    public Long getUserId(){return userId;}
    public void setUserId(Long userId){ this.userId=userId;}
}
