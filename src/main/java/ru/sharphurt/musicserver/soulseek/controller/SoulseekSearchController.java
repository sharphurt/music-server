package ru.sharphurt.musicserver.soulseek.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sharphurt.musicserver.soulseek.dto.SearchCreatedResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SearchRequestDto;
import ru.sharphurt.musicserver.soulseek.service.SoulseekSearchService;

@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SoulseekSearchController {

    private final SoulseekSearchService searchService;

    @PostMapping("/search")
    public ResponseEntity<SearchCreatedResponseDto> createSearchTask(@RequestBody SearchRequestDto request) {
        try {
            String searchId = searchService.initiateSearch(request.query());
            return ResponseEntity.ok(new SearchCreatedResponseDto(searchId, "Search initiated [%s]".formatted(searchId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchCreatedResponseDto(null, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/search/{searchId}/results")
    public ResponseEntity<String> getSearchResults(@PathVariable String searchId) {
        try {
            String results = searchService.fetchSearchResults(searchId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch results: " + e.getMessage() + "\"}");
        }
    }
}
