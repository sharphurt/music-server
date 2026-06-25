package ru.sharphurt.musicserver.mediametadata.itunes.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.mediametadata.dataenrichment.EnrichmentService;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.mediametadata.itunes.mapper.ITunesMapper;
import ru.sharphurt.musicserver.mediametadata.search.SearchProvider;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesSearchService implements SearchProvider {

    private final ITunesRestService restClient;
    private final ITunesMapper mapper;

    @Qualifier("aliasesEnrichmentService")
    private final EnrichmentService<TrackEntity> enrichmentService;

    @Override
    @Cacheable(value = "tracksByQuery", key = "#query + ':' + #page + ':' + #limit")
    public SearchResult<TrackEntity> searchTracksBy(String query, long limit, long page) {
        SearchResult<ITunesTrackDto> searchResult = restClient.search(query, limit, page);
        if (searchResult == null) {
            return null;
        }

        List<TrackEntity> tracks = mapper.mapAllToTrackDto(searchResult.getResults());
        return SearchResult.<TrackEntity>builder()
            .resultCount(searchResult.getResultCount())
            .results(tracks)
            .build();
    }

    @Override
    @Cacheable(value = "trackById", key = "#id")
    public TrackEntity searchTrackById(long id) {
        ITunesTrackDto track = restClient.lookup(id, null);
        if (track == null) {
            log.info("Не найдено трека по id {}", id);
            return null;
        }

        return enrichmentService.enrich(mapper.mapToTrackDto(track));
    }

    @Override
    public AlbumEntity searchAlbumById(long id) {
        return null;
    }
}