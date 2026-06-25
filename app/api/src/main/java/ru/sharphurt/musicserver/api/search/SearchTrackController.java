package ru.sharphurt.musicserver.api.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.api.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.api.search.dto.SearchResponseDto;
import ru.sharphurt.musicserver.mediametadata.search.SearchProvider;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchTrackController {

    private final SearchProvider searchProvider;

    public SearchTrackController(@Qualifier("ITunesSearchService") SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @PostMapping("/track")
    public SearchResponseDto<TrackEntity> searchTrack(@RequestBody SearchRequestDto request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            return SearchResponseDto.empty(request);
        }

        SearchResult<TrackEntity> searchResult = searchProvider.searchTracksBy(request.query(),
            request.limit(),
            request.page());

        if (searchResult == null) {
            log.info("По запросу {} не найдено результатов", request);
            return SearchResponseDto.empty(request);
        }

        long startIndex = (request.page() - 1) * request.limit();

        return SearchResponseDto.withContent(
            request.type(),
            searchResult.getResults(),
            startIndex + searchResult.getResultCount(),
            request.query(),
            request.limit(),
            request.page()
        );
    }
}
