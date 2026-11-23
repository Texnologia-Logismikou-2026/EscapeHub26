package org.example.escapehubbackend.repository;

import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionSessionRepository extends JpaRepository<MissionSession, Long> {
    Optional<MissionSession> findFirstByTeamIdAndMissionIdAndStatusOrderByStartedAtDesc(
            Long teamId,
            Long missionId,
            SessionStatus status
    );
}
