package org.example.escapehubbackend.service;

import org.example.escapehubbackend.dto.CreateTeamRequest;
import org.example.escapehubbackend.dto.TeamResponse;
import org.example.escapehubbackend.model.Team;
import org.example.escapehubbackend.model.User;
import org.example.escapehubbackend.repository.TeamRepository;
import org.example.escapehubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required");
        }
        if (teamRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team name already exists");
        }

        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Team team = new Team(request.getName().trim(), creator.getId());
        team.getMembers().add(creator);

        Team savedTeam = teamRepository.save(team);
        return mapToResponse(savedTeam);
    }

    @Transactional
    public TeamResponse joinTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (alreadyMember) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in this team");
        }

        team.getMembers().add(user);
        Team updatedTeam = teamRepository.save(team);
        return mapToResponse(updatedTeam);
    }

    @Transactional
    public TeamResponse joinTeamByName(String teamName, Long userId) {
        if (teamName == null || teamName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required");
        }
        Team team = teamRepository.findByName(teamName.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + teamName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));

        if (!alreadyMember) {
            team.getMembers().add(user);
            team = teamRepository.save(team);
        }
        return mapToResponse(team);
    }

    private TeamResponse mapToResponse(Team team) {
        List<TeamResponse.MemberInfo> memberInfos = team.getMembers().stream()
                .map(user -> {
                    String role = user.getId().equals(team.getCreatorId()) ? "CREATOR" : "MEMBER";
                    return new TeamResponse.MemberInfo(user.getId(), user.getUsername(), role);
                })
                .toList();

        return new TeamResponse(team.getId(), team.getName(), team.getCreatorId(), memberInfos);
    }
}