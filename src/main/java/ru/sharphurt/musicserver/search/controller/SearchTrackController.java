package ru.sharphurt.musicserver.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.search.service.SearchTrackService;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
public class SearchTrackController {

    private final SearchTrackService searchService;

    @GetMapping("/track")
    public SearchResponseDto<TrackDto> searchTrack(@RequestBody SearchRequestDto request) {
        log.info("Received search request: {}", request);
        return searchService.searchTracks(request);
    }
}
