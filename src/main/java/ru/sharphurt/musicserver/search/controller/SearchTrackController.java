package ru.sharphurt.musicserver.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;
import ru.sharphurt.musicserver.search.service.SearchTrackService;

@RestController
@RequestMapping("/api/search")
@Slf4j
@RequiredArgsConstructor
public class SearchTrackController {

    private final SearchTrackService searchService;

    @PostMapping("/track")
    public SearchResponseDto<TrackEntity> searchTrack(@RequestBody SearchRequestDto request) {
        return searchService.searchTracks(request);
    }
}
