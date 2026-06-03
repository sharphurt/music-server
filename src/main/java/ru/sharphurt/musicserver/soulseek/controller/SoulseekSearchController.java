package ru.sharphurt.musicserver.soulseek.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sharphurt.musicserver.soulseek.dto.MatchCandidateDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchTaskDto;
import ru.sharphurt.musicserver.soulseek.service.SoulseekSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SoulseekSearchController {

    private final SoulseekSearchService searchService;

    @PostMapping("/search")
    public ResponseEntity<List<SoulseekSearchTaskDto>> createSearchTask(@RequestParam Long trackId) {
        List<SoulseekSearchTaskDto> searchTasks = searchService.initiateSearch(trackId);
        return ResponseEntity.ok(searchTasks);
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<MatchCandidateDto>> getSearchResults(@RequestParam Long trackId) {
        List<MatchCandidateDto> results = searchService.fetchSearchResults(trackId);
        return ResponseEntity.ok(results);
    }
}
