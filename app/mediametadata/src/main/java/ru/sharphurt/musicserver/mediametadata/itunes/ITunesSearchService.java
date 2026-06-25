package ru.sharphurt.musicserver.mediametadata.itunes;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.common.entity.ArtistEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.mediametadata.dataenrichment.EnrichmentService;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesAlbumTracksResult;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesArtistDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.mediametadata.itunes.mapper.ITunesAlbumMapper;
import ru.sharphurt.musicserver.mediametadata.itunes.mapper.ITunesArtistMapper;
import ru.sharphurt.musicserver.mediametadata.itunes.mapper.ITunesTrackMapper;
import ru.sharphurt.musicserver.mediametadata.itunes.rest.ITunesLookupRestClient;
import ru.sharphurt.musicserver.mediametadata.itunes.rest.ITunesSearchRestClient;
import ru.sharphurt.musicserver.mediametadata.search.SearchProvider;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesSearchService implements SearchProvider {

    private final ITunesSearchRestClient searchRestClient;
    private final ITunesLookupRestClient lookupRestClient;
    private final ITunesTrackMapper trackMapper;
    private final ITunesAlbumMapper albumMapper;
    private final ITunesArtistMapper artistMapper;
    private final EnrichmentService<TrackEntity> enrichmentService;

    @Override
    @Cacheable(value = "tracksByQuery", key = "#query + ':' + #page + ':' + #limit")
    public SearchResult<TrackEntity> searchTracksBy(String query, long limit, long page) {
        SearchResult<ITunesTrackDto> searchResult = searchRestClient.searchTracks(query, limit, page);
        if (searchResult == null) {
            return null;
        }

        List<TrackEntity> tracks = trackMapper.toTrackEntityList(searchResult.getResults());
        return SearchResult.<TrackEntity>builder()
            .resultCount(searchResult.getResultCount())
            .results(tracks)
            .build();
    }

    @Override
    @Cacheable(value = "trackById", key = "#id")
    public TrackEntity searchTrackById(long id) {
        ITunesTrackDto track = lookupRestClient.getTrack(id);
        if (track == null) {
            log.info("Не найдено трека по id {}", id);
            return null;
        }

//        return enrichmentService.enrich(trackMapper.toTrackEntity(track));
        return trackMapper.toTrackEntity(track);
    }

    @Override
    public Pair<AlbumEntity, List<TrackEntity>> searchAlbumWithTracksById(long id) {
        ITunesAlbumTracksResult albumWithTracks = lookupRestClient.getAlbumWithTracks(id);

        if (albumWithTracks == null) {
            log.info("Не найдено данных об альбоме по id {}", id);
            return null;
        }

        AlbumEntity album = albumMapper.toAlbumEntity(albumWithTracks.album());
        List<TrackEntity> tracks = trackMapper.toTrackEntityList(albumWithTracks.tracks());

        return Pair.of(album, tracks);
    }

    @Override
    public ArtistEntity searchArtistById(long id) {
        ITunesArtistDto artist = lookupRestClient.getArtist(id);
        if (artist == null) {
            log.info("Не найдено исполнителя по id {}", id);
            return null;
        }

        return artistMapper.toArtistEntity(artist);
    }
}