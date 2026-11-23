package org.example.escapehubbackend;

import org.example.escapehubbackend.dto.MissionSessionResponse;
import org.example.escapehubbackend.dto.StartSessionRequest;
import org.example.escapehubbackend.model.SessionStatus;
import org.example.escapehubbackend.service.MissionSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class MissionSessionTest {

    @Autowired
    private MissionSessionService missionSessionService;

    @Test
    void shouldStartResumeAndRetrieveMissionSession() {
        StartSessionRequest request = new StartSessionRequest(1L, 10L);

        MissionSessionResponse created = missionSessionService.startOrResumeSession(request);
        assertNotNull(created.getId());
        assertEquals(1L, created.getTeamId());
        assertEquals(10L, created.getMissionId());
        assertEquals(SessionStatus.IN_PROGRESS, created.getStatus());
        assertNotNull(created.getStartedAt());

        MissionSessionResponse resumed = missionSessionService.startOrResumeSession(request);
        assertEquals(created.getId(), resumed.getId());

        MissionSessionResponse fetched = missionSessionService.getSession(created.getId());
        assertEquals(created.getId(), fetched.getId());
        assertEquals(SessionStatus.IN_PROGRESS, fetched.getStatus());
    }
}
