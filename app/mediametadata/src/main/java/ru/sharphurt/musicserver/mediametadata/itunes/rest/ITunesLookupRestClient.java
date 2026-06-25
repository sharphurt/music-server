package ru.sharphurt.musicserver.mediametadata.itunes.rest;

import static ru.sharphurt.musicserver.common.GlobalConstants.ITUNES_BASE_URL;
import static ru.sharphurt.musicserver.common.GlobalConstants.USER_AGENT;
import static ru.sharphurt.musicserver.mediametadata.util.JavascriptAwareConverter.javascriptAwareConverter;

import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.mediametadata.itunes.ITunesQueryBuilder;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesAlbumTracksResult;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesArtistDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesCollectionDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesResultDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.mediametadata.itunes.ITunesEntityType;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@Slf4j
@Service
public class ITunesLookupRestClient {

    private final RestClient restClient;

    public ITunesLookupRestClient(SimpleClientHttpRequestFactory httpRequestFactory) {
        this.restClient = RestClient.builder()
            .baseUrl(ITUNES_BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .configureMessageConverters(builder ->
                builder.addCustomConverter(javascriptAwareConverter()))
            .requestFactory(httpRequestFactory)
            .build();
    }

    public ITunesAlbumTracksResult getAlbumWithTracks(long albumId, String country) {
        List<ITunesResultDto> results = lookup(albumId, ITunesEntityType.SONG, country);
        if (results == null || results.isEmpty()) {
            log.warn("ITunes: нет результатов для albumId={}", albumId);
            return null;
        }

        ITunesCollectionDto album = results.stream()
            .filter(ITunesCollectionDto.class::isInstance)
            .map(ITunesCollectionDto.class::cast)
            .findFirst()
            .orElse(null);

        List<ITunesTrackDto> tracks = results.stream()
            .filter(ITunesTrackDto.class::isInstance)
            .map(ITunesTrackDto.class::cast)
            .toList();

        return new ITunesAlbumTracksResult(album, tracks);
    }

    public ITunesAlbumTracksResult getAlbumWithTracks(long albumId) {
        return getAlbumWithTracks(albumId, null);
    }

    public ITunesTrackDto getTrack(long trackId, String country) {
        List<ITunesResultDto> results = lookup(trackId, ITunesEntityType.SONG, country);
        if (results == null || results.isEmpty()) {
            log.warn("ITunes: трек не найден. trackId={}", trackId);
            return null;
        }

        return results.stream()
            .filter(ITunesTrackDto.class::isInstance)
            .map(ITunesTrackDto.class::cast)
            .findFirst()
            .orElse(null);
    }

    public ITunesTrackDto getTrack(long trackId) {
        return getTrack(trackId, null);
    }

    public ITunesArtistDto getArtist(long artistId, String country) {
        List<ITunesResultDto> results = lookup(artistId, ITunesEntityType.ARTIST, country);
        if (results == null || results.isEmpty()) {
            log.warn("ITunes: артист не найден. artistId={}", artistId);
            return null;
        }

        return results.stream()
            .filter(ITunesArtistDto.class::isInstance)
            .map(ITunesArtistDto.class::cast)
            .findFirst()
            .orElse(null);
    }

    public ITunesArtistDto getArtist(long artistId) {
        return getArtist(artistId, null);
    }

    private List<ITunesResultDto> lookup(long id, ITunesEntityType entityType, String country) {
        URI uri = ITunesQueryBuilder.buildLookupUrl(id, entityType, country);
        log.info("ITunes LOOKUP [{}]: {}", entityType.getApiName(), uri);

        try {
            ResponseEntity<SearchResult<ITunesResultDto>> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

            if (response.getStatusCode().isError() || response.getBody() == null) {
                log.error("Ошибка lookup в ITunes. Статус {}. id={}", response.getStatusCode(), id);
                return null;
            }

            return response.getBody().getResults();
        } catch (Exception e) {
            log.error("Ошибка при выполнении lookup по iTunes. id={}", id, e);
            return null;
        }
    }
}
