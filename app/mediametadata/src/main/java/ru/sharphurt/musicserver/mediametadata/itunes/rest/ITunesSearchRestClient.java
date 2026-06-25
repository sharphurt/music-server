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
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesArtistDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesCollectionDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesResultDto;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.mediametadata.itunes.ITunesEntityType;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@Slf4j
@Service
public class ITunesSearchRestClient {

    private final RestClient restClient;

    public ITunesSearchRestClient(SimpleClientHttpRequestFactory httpRequestFactory) {
        this.restClient = RestClient.builder()
            .baseUrl(ITUNES_BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .configureMessageConverters(builder ->
                builder.addCustomConverter(javascriptAwareConverter()))
            .requestFactory(httpRequestFactory)
            .build();
    }

    public SearchResult<ITunesTrackDto> searchTracks(String query, long limit, long page) {
        return search(query, ITunesEntityType.SONG, limit, page, ITunesTrackDto.class);
    }

    public SearchResult<ITunesCollectionDto> searchAlbums(String query, long limit, long page) {
        return search(query, ITunesEntityType.ALBUM, limit, page, ITunesCollectionDto.class);
    }

    public SearchResult<ITunesArtistDto> searchArtists(String query, long limit, long page) {
        return search(query, ITunesEntityType.ARTIST, limit, page, ITunesArtistDto.class);
    }

    private <T extends ITunesResultDto> SearchResult<T> search(String query, ITunesEntityType entityType, long limit, long page, Class<T> type) {
        try {
            URI uri = ITunesQueryBuilder.buildSearchUrl(query, entityType, limit, page);
            log.info("ITunes SEARCH [{}]: {}", entityType.getApiName(), uri);

            ResponseEntity<SearchResult<ITunesResultDto>> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

            if (response.getStatusCode().isError() || response.getBody() == null) {
                log.error("Ошибка поиска в ITunes. Статус {}. Запрос: {}", response.getStatusCode(), query);
                return null;
            }

            SearchResult<ITunesResultDto> body = response.getBody();
            log.info("ITunes SEARCH [{}]: Найдено {} результатов по запросу '{}'",
                entityType.getApiName(), body.getResultCount(), query);

            List<T> filtered = body.getResults().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();

            return new SearchResult<>(body.getResultCount(), filtered);
        } catch (Exception e) {
            log.error("Ошибка при выполнении поиска по iTunes [{}]. Запрос: {}", entityType.getApiName(), query, e);
            return null;
        }
    }

}