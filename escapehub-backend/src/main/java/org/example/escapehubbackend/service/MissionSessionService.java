package org.example.escapehubbackend.service;

import org.example.escapehubbackend.dto.MissionSessionResponse;
import org.example.escapehubbackend.dto.StartSessionRequest;
import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.SessionStatus;
import org.example.escapehubbackend.repository.MissionSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MissionSessionService {

    private final MissionSessionRepository missionSessionRepository;

    public MissionSessionService(MissionSessionRepository missionSessionRepository) {
        this.missionSessionRepository = missionSessionRepository;
    }

    public MissionSessionResponse startOrResumeSession(StartSessionRequest request) {
        if (request == null || request.getTeamId() == null || request.getMissionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId and missionId are required");
        }

        MissionSession session = missionSessionRepository
                .findFirstByTeamIdAndMissionIdAndStatusOrderByStartedAtDesc(
                        request.getTeamId(),
                        request.getMissionId(),
                        SessionStatus.IN_PROGRESS
                )
                .orElseGet(() -> new MissionSession(request.getTeamId(), request.getMissionId()));

        MissionSession saved = missionSessionRepository.save(session);
        return MissionSessionResponse.fromEntity(saved);
    }

    public MissionSessionResponse getSession(Long sessionId) {
        MissionSession session = missionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "MissionSession not found with id: " + sessionId
                ));
        return MissionSessionResponse.fromEntity(session);
    }
}