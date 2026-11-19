package org.example.escapehubbackend.controller;

import org.example.escapehubbackend.dto.CreateTeamRequest;
import org.example.escapehubbackend.dto.JoinTeamRequest;
import org.example.escapehubbackend.dto.TeamResponse;
import org.example.escapehubbackend.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")

public class TeamController {

    private final TeamService teamService;

    public  TeamController(TeamService teamService){
        this.teamService=teamService;
    }

    @PostMapping("/create")
    public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamRequest request){
        TeamResponse created=teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);


    }
    @PostMapping("/{teamId}/join")
    public ResponseEntity <TeamResponse> joinTeam(@PathVariable Long teamId, @RequestBody JoinTeamRequest request){
        TeamResponse updated=teamService.joinTeam(teamId,request.getUserId());
        return ResponseEntity.ok(updated);
    }
}
