package org.example.escapehubbackend.dto;

import java.util.List;

public class TeamResponse {
    private Long id;
    private String name;
    private Long creatorId;
    private List<MemberInfo> members;

    public TeamResponse(Long id, String name, Long creatorId, List<MemberInfo> members){
        this.name=name;
        this.id=id;
        this.creatorId=creatorId;
        this.members=members;

    }

    public Long getId(){return id;}
    public String getName(){return name;}
    public Long getCreatorId(){return creatorId;}
    public List<MemberInfo> getMembers(){return members;}

    public static class MemberInfo{
        private Long userId;
        private String username;
        private String role;

        public MemberInfo(Long userId,String username,String role){
            this.userId=userId;
            this.username=username;
            this.role=role;
        }
        public Long getUserId(){return userId;}
        public String getUsername(){return username;}
        public String getRole(){return role;}
    }

}
