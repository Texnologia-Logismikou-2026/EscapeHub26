package org.example.escapehubbackend.service;

import org.example.escapehubbackend.dto.CreateTeamRequest;
import org.example.escapehubbackend.dto.TeamResponse;
import org.example.escapehubbackend.model.Team;
import org.example.escapehubbackend.model.User;
import org.example.escapehubbackend.repository.TeamRepository;
import org.example.escapehubbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void testCreateTeamSuccess() {
        User creator = new User("nick", "pass123", "nick@test.gr");
        creator.setId(1L);

        Team savedTeam = new Team("AlphaSquad", 1L);
        savedTeam.setId(10L);
        savedTeam.getMembers().add(creator);

        when(teamRepository.findByName("AlphaSquad")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        TeamResponse response = teamService.createTeam(new CreateTeamRequest("AlphaSquad", 1L));

        assertEquals(10L, response.getId());
        assertEquals("AlphaSquad", response.getName());
        assertEquals(1, response.getMembers().size());
        assertEquals("CREATOR", response.getMembers().get(0).getRole());
    }

    @Test
    void testJoinTeamSuccess() {
        User creator = new User("nick", "pass123", "nick@test.gr");
        creator.setId(1L);

        User player2 = new User("maria", "pass123", "maria@test.gr");
        player2.setId(2L);

        Team team = new Team("AlphaSquad", 1L);
        team.setId(10L);
        team.getMembers().add(creator);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse response = teamService.joinTeam(10L, 2L);

        assertEquals(2, response.getMembers().size());
        TeamResponse.MemberInfo member2 = response.getMembers().stream()
                .filter(m -> m.getUserId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertEquals("MEMBER", member2.getRole());
    }

    @Test
    void testJoinTeamAlreadyMember() {
        User creator = new User("nick", "pass123", "nick@test.gr");
        creator.setId(1L);

        Team team = new Team("AlphaSquad", 1L);
        team.setId(10L);
        team.getMembers().add(creator);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));

        assertThrows(ResponseStatusException.class, () -> teamService.joinTeam(10L, 1L));
        verify(teamRepository, never()).save(any());
    }
}