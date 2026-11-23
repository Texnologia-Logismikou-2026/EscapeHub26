package org.example.escapehubbackend.repository;

import org.example.escapehubbackend.model.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {
    List<Puzzle> findByMissionId(Long missionId);
}