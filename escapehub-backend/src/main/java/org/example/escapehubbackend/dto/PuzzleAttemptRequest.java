package org.example.escapehubbackend.dto;

import java.util.List;

import java.util.Map;

public class PuzzleAttemptRequest {

    private Long userId;
    private String answer;
    private List<String> actionSequence;
    private  Map<String,String> externalApiSnapshots;

    public PuzzleAttemptRequest(Long userId,String answer){

        this.userId=userId;
        this.answer=answer;

    }

    public PuzzleAttemptRequest(){


    }

    public Long getUserId(){return userId;}
    public void setUserId(Long userId){this.userId=userId;}


    public String getAnswer(){return answer;}
    public void setAnswer(String answer){this.answer=answer;}

    public List<String> getActionSequence(){return actionSequence;}
    public void setActionSequence(List<String> actionSequence){this.actionSequence=actionSequence;}

    public Map<String,String> getExternalApiSnapshots(){return externalApiSnapshots;}
    public void setExternalApiSnapshots(Map<String,String> externalApiSnapshots){this.externalApiSnapshots=externalApiSnapshots;}



    }

