package org.example.escapehubbackend.dto;

public class CreateTeamRequest {
    private String name;
    private Long creatorId;


    public CreateTeamRequest() {

    }


        public CreateTeamRequest(String name,Long creatorId){
            this.name=name;
            this.creatorId=creatorId;
        }


    public String getName(){return name;}
    public void setName(String name){this.name=name;}

    public Long getCreatorId(){return creatorId;}
    public void setCreatorId(Long creatorId){this.creatorId=creatorId;}
}
