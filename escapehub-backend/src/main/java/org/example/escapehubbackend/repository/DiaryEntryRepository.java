package org.example.escapehubbackend.repository;

import org.example.escapehubbackend.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {
    List<DiaryEntry> findByTeamIdOrderByCreatedAtDesc(Long teamId);
}