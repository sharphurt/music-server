package ru.sharphurt.musicserver.search.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.search.SearchProvider;
import ru.sharphurt.musicserver.search.dto.RawSearchResultDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;
import ru.sharphurt.musicserver.search.dto.TrackDto;
import ru.sharphurt.musicserver.search.enrichment.EnrichmentExecutionService;

import java.util.Comparator;
import java.util.List;

@Service
public class SearchTrackService {

    private final EnrichmentExecutionService<TrackDto> trackEnrichmentExecutionService;

    private final SearchProvider searchProvider;

    public SearchTrackService(EnrichmentExecutionService<TrackDto> trackEnrichmentExecutionService,
                              @Qualifier("ITunesSearchService") SearchProvider searchProvider) {
        this.trackEnrichmentExecutionService = trackEnrichmentExecutionService;
        this.searchProvider = searchProvider;
    }

    public SearchResponseDto<TrackDto> searchTracks(@NonNull SearchRequestDto request) {
        RawSearchResultDto<TrackDto> rawSearchResults = searchProvider.searchTracks(request);
        List<TrackDto> tracks = trackEnrichmentExecutionService.enrich(rawSearchResults.getEntities())
                .stream()
                .sorted(Comparator.comparingLong(TrackDto::getPlaycounts).thenComparing(TrackDto::getDuration).reversed())
                .toList();

        return SearchResponseDto.<TrackDto>builder()
                .entities(tracks)
                .totalResults(rawSearchResults.getTotalCount())
                .page(rawSearchResults.getPage())
                .pageSize(rawSearchResults.getPageSize())
                .query(rawSearchResults.getQuery())
                .build();
    }
}
