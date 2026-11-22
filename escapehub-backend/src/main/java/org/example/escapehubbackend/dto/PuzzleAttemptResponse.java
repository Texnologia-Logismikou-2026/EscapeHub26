package org.example.escapehubbackend.dto;

import java.util.List;

public class PuzzleAttemptResponse {

    private boolean solved;
    private boolean alreadySolved;
    private String revealedClue;
    private List<String> newlyUnlockedPuzzleCodes;
    private boolean missionComplete;
    private Long durationSeconds;

    public PuzzleAttemptResponse(){


    }
    public PuzzleAttemptResponse(
            boolean solved,
            boolean alreadySolved,
            String revealedClue,
            List<String> newlyUnlockedPuzzleCodes,
            boolean missionComplete,
            Long durationSeconds


    ){
        this.alreadySolved=alreadySolved;
        this.solved=solved;
        this.revealedClue=revealedClue;
        this.newlyUnlockedPuzzleCodes=newlyUnlockedPuzzleCodes;
        this.missionComplete=missionComplete;
        this.durationSeconds=durationSeconds;
    }
    public boolean isSolved(){return solved;}
    public void setSolved(boolean solved){this.solved=solved;}

    public boolean isAlreadySolved(){return alreadySolved;}
    public void setAlreadySolved(boolean alreadySolved){this.alreadySolved=alreadySolved;}

    public String isRevealedClue(){return revealedClue;}
    public void setRevealedClue(String revealedClue){this.revealedClue=revealedClue;}

    public List<String> isNewlyUnlockedPuzzleCodes(){return newlyUnlockedPuzzleCodes;}
    public void setNewlyUnlockedPuzzleCodes(List<String> newlyUnlockedPuzzleCodes){this.newlyUnlockedPuzzleCodes=newlyUnlockedPuzzleCodes;}

    public boolean isMissionComplete(){return missionComplete;}
    public void setMissionComplete(boolean missionComplete){this.missionComplete=missionComplete;}

    public Long isDurationSeconds(){return durationSeconds;}
    public void getDurationSeconds(Long durationSeconds){this.durationSeconds=durationSeconds;}

}
