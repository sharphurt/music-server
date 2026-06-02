package ru.sharphurt.musicserver.itunes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dataenrichment.EnrichmentExecutionService;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesSearchResponseDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.search.SearchProvider;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesSearchService implements SearchProvider {

    private final ITunesRestService restClient;
    private final ITunesMappingService mapper;
    private final EnrichmentExecutionService<TrackDto> trackEnrichmentExecutionService;


    @Override
    public SearchResponseDto<TrackDto> searchTracksBy(SearchRequestDto request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            return SearchResponseDto.empty(request);
        }

        Optional<ITunesSearchResponseDto> response = restClient.search(request);
        if (response.isEmpty()) {
            return SearchResponseDto.empty(request);
        }

        List<TrackDto> tracks = mapper.mapToTrackDto(response.get().results());
        long startIndex = (request.page() - 1) * request.limit();

        return SearchResponseDto.withContent(
                request.type(),
                trackEnrichmentExecutionService.enrich(tracks),
                startIndex + response.get().resultCount(),
                request.query(),
                request.limit(),
                request.page()
        );
    }

    @Override
    public TrackDto searchTrackById(long id) {
        log.info("Поиск трека по id {}", id);

        // todo: db caching
        Optional<ITunesTrackDto> track = restClient.lookup(id, null);
        if (track.isEmpty()) {
            log.info("Не найдено трека по id {}", id);
            throw new RuntimeException("Трек с id " + id + " не найден");
        }

        return trackEnrichmentExecutionService.enrich(mapper.mapToTrackDto(track.get()));
    }
}