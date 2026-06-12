package ru.sharphurt.musicserver.soulseek.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskSearchTaskDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.service.SlskSearchService;
import ru.sharphurt.musicserver.soulseek.service.SlskkDownloadService;

@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SlskApiController {

    private final SlskSearchService searchService;

    private final SlskkDownloadService downloadService;

    @PostMapping("/search")
    public ResponseEntity<List<SlskSearchTaskDto>> createSearchTask(@RequestParam Long trackId) {
        List<SlskSearchTaskDto> searchTasks = searchService.initiateSearch(trackId);
        return ResponseEntity.ok(searchTasks);
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<SlskFileScoreDto>> getSearchResults(@RequestParam Long trackId,
        @RequestParam(defaultValue = "200") Integer maxResults) {
        List<SlskFileScoreDto> results = searchService.fetchSearchResults(trackId, maxResults);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/download")
    public ResponseEntity<SlskTransfersResponseDto> enqueueDownload(
        @RequestBody SlskDownloadRequestDto downloadRequestDto) {
        return ResponseEntity.ok(downloadService.enqueueDownload(downloadRequestDto));
    }

    @GetMapping("/download/list")
    public ResponseEntity<List<SlskFileTransferDto>> getDownloads() {
        List<SlskFileTransferDto> downloads = downloadService.getDownloads();
        if (downloads == null || downloads.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(downloads);
    }
}
