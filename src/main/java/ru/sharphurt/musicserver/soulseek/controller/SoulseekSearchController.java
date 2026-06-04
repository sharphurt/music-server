package ru.sharphurt.musicserver.soulseek.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileScoreDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchTaskDto;
import ru.sharphurt.musicserver.soulseek.dto.TransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.service.SoulseekDownloadService;
import ru.sharphurt.musicserver.soulseek.service.SoulseekSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SoulseekSearchController {

    private final SoulseekSearchService searchService;

    private final SoulseekDownloadService downloadService;

    @PostMapping("/search")
    public ResponseEntity<List<SoulseekSearchTaskDto>> createSearchTask(@RequestParam Long trackId) {
        List<SoulseekSearchTaskDto> searchTasks = searchService.initiateSearch(trackId);
        return ResponseEntity.ok(searchTasks);
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<SoulseekFileScoreDto>> getSearchResults(@RequestParam Long trackId, @RequestParam(defaultValue = "200") Integer maxResults) {
        List<SoulseekFileScoreDto> results = searchService.fetchSearchResults(trackId, maxResults);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/download")
    public ResponseEntity<TransfersResponseDto> enqueueDownload(@RequestBody SoulseekFileNodeDto fileNodeDto) {
        return ResponseEntity.ok(downloadService.enqueueDownload(fileNodeDto));
    }
}
