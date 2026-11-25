package org.example.escapehubbackend.service;

import org.example.escapehubbackend.dto.PuzzleAttemptRequest;
import org.example.escapehubbackend.dto.PuzzleAttemptResponse;
import org.example.escapehubbackend.engine.MissionRuleEngine;
import org.example.escapehubbackend.engine.MissionRuleEngine.AttemptOutcome;
import org.example.escapehubbackend.engine.PuzzleAttemptContext;
import org.example.escapehubbackend.model.DiaryEntry;
import org.example.escapehubbackend.model.MissionSession;
import org.example.escapehubbackend.model.Puzzle;
import org.example.escapehubbackend.model.User;
import org.example.escapehubbackend.repository.DiaryEntryRepository;
import org.example.escapehubbackend.repository.MissionSessionRepository;
import org.example.escapehubbackend.repository.PuzzleRepository;
import org.example.escapehubbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MissionGameplayService {

    private final MissionSessionRepository sessionRepository;
    private final PuzzleRepository puzzleRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final UserRepository userRepository;
    private final MissionRuleEngine ruleEngine;

    public MissionGameplayService(
            MissionSessionRepository sessionRepository,
            PuzzleRepository puzzleRepository,
            DiaryEntryRepository diaryEntryRepository,
            UserRepository userRepository

    ) {
        this.sessionRepository = sessionRepository;
        this.puzzleRepository = puzzleRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.userRepository = userRepository;
        this.ruleEngine = new MissionRuleEngine();
    }

    @Transactional
    public PuzzleAttemptResponse submitAttempt(Long sessionId, Long puzzleId, PuzzleAttemptRequest request) {
        MissionSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("MissionSession not found: " + sessionId));

        Puzzle targetPuzzle = puzzleRepository.findById(puzzleId)
                .orElseThrow(() -> new IllegalArgumentException("Puzzle not found: " + puzzleId));

        if (!targetPuzzle.getMissionId().equals(session.getMissionId())) {
            throw new IllegalArgumentException("Puzzle does not belong to the active mission");
        }

        List<Puzzle> allMissionPuzzles = puzzleRepository.findByMissionId(session.getMissionId());

        PuzzleAttemptContext context = new PuzzleAttemptContext(
                request != null ? request.getUserId() : null,
                request != null ? request.getAnswer() : null,
                request != null ? request.getActionSequence() : null,
                request != null ? request.getExternalApiSnapshots() : null
        );

        AttemptOutcome outcome = ruleEngine.evaluateAttempt(
                session,
                targetPuzzle,
                allMissionPuzzles,
                context,
                LocalDateTime.now()
        );

        if (outcome.solved() && !outcome.alreadySolved() && outcome.revealedClue() != null) {
            String authorName = resolveAuthorName(request != null ? request.getUserId() : null);
            DiaryEntry entry = new DiaryEntry(
                    session.getTeamId(),
                    authorName,
                    "[" + targetPuzzle.getCode() + "] " + outcome.revealedClue()
            );
            diaryEntryRepository.save(entry);
        }

        sessionRepository.save(session);

        List<String> unlockedCodes = outcome.newlyUnlockedPuzzles().stream()
                .map(Puzzle::getCode)
                .toList();

        return new PuzzleAttemptResponse(
                outcome.solved(),
                outcome.alreadySolved(),
                outcome.revealedClue(),
                unlockedCodes,
                outcome.missionCompleted(),
                session.getDurationSeconds()
        );
    }

    private String resolveAuthorName(Long userId) {
        if (userId == null) {
            return "System";
        }
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Player #" + userId);
    }
}