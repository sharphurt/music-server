package ru.sharphurt.musicserver.itunes.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dataenrichment.EnrichmentService;
import ru.sharphurt.musicserver.locallibrary.enitiy.TrackEntity;
import ru.sharphurt.musicserver.itunes.dto.ITunesSearchResponseDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.itunes.mapper.ITunesMapper;
import ru.sharphurt.musicserver.search.SearchProvider;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesSearchService implements SearchProvider {

    private final ITunesRestService restClient;
    private final ITunesMapper mapper;

    @Qualifier("aliasesEnrichmentService")
    private final EnrichmentService<TrackEntity> enrichmentService;

    @Override
    @Cacheable(value = "tracksByQuery", key = "#request.query() + ':' + #request.page() + ':' + #request.limit() + ':' + #request.type()")
    public SearchResponseDto<TrackEntity> searchTracksBy(SearchRequestDto request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            return SearchResponseDto.empty(request);
        }

        Optional<ITunesSearchResponseDto> response = restClient.search(request);
        if (response.isEmpty()) {
            return SearchResponseDto.empty(request);
        }

        List<TrackEntity> tracks = mapper.mapAllToTrackDto(response.get().results());
        long startIndex = (request.page() - 1) * request.limit();

        return SearchResponseDto.withContent(
            request.type(),
            tracks,
            startIndex + response.get().resultCount(),
            request.query(),
            request.limit(),
            request.page()
        );
    }

    @Override
    @Cacheable(value = "trackById", key = "#id")
    public TrackEntity searchTrackById(long id) {
        log.info("Поиск трека по id {}", id);
        Optional<ITunesTrackDto> track = restClient.lookup(id, null);
        if (track.isEmpty()) {
            log.info("Не найдено трека по id {}", id);
            throw new RuntimeException("Трек с id " + id + " не найден");
        }

        return enrichmentService.enrich(mapper.mapToTrackDto(track.get()));
    }
}