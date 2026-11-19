package org.example.escapehubbackend.controller;

import org.example.escapehubbackend.dto.CreateDiaryEntryRequest;
import org.example.escapehubbackend.model.DiaryEntry;
import org.example.escapehubbackend.repository.DiaryEntryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryEntryRepository diaryEntryRepository;

    public DiaryController(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    @GetMapping("/team/{teamId}")
    public List<DiaryEntry> getTeamDiary(@PathVariable Long teamId) {
        return diaryEntryRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }

    @PostMapping
    public DiaryEntry addDiaryEntry(@RequestBody CreateDiaryEntryRequest request) {
        DiaryEntry entry = new DiaryEntry(
                request.getTeamId(),
                request.getAuthorName(),
                request.getContent()
        );
        return diaryEntryRepository.save(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiaryEntry(@PathVariable Long id) {
        if (!diaryEntryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        diaryEntryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}