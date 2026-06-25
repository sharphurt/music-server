package ru.sharphurt.musicserver.mediametadata.itunes.service;

import static ru.sharphurt.musicserver.common.GlobalConstants.ITUNES_BASE_URL;
import static ru.sharphurt.musicserver.common.GlobalConstants.USER_AGENT;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.mediametadata.itunes.ITunesQueryBuilder;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.mediametadata.search.SearchResult;

@Slf4j
@Service
public class ITunesRestService {

    private final RestClient restClient;

    public ITunesRestService(SimpleClientHttpRequestFactory httpRequestFactory) {
        this.restClient = RestClient.builder()
            .baseUrl(ITUNES_BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .configureMessageConverters(builder ->
                builder.addCustomConverter(javascriptAwareConverter()))
            .requestFactory(httpRequestFactory)
            .build();
    }

    private static JacksonJsonHttpMessageConverter javascriptAwareConverter() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();
        converter.setSupportedMediaTypes(
            List.of(new MediaType("text", "javascript", StandardCharsets.UTF_8)));
        return converter;
    }

    public SearchResult<ITunesTrackDto> search(String query, long limit, long page) {
        try {
            URI uri = ITunesQueryBuilder.buildTrackSearchUrl(query, limit, page);

            log.info("ITunes SEARCH: {}", uri);
            ResponseEntity<SearchResult<ITunesTrackDto>> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

            if (response.getStatusCode().isError() || response.getBody() == null) {
                log.error("Ошибка поиска в ITunes. Статус {}. Запрос: {}", response.getStatusCode(),
                    query);
                return null;
            }

            log.info("ITunes SEARCH: Найдено {} результатов по запросу '{}'", response.getBody().getResultCount(), query);

            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка при выполнении поиска по iTunes. Запрос: {}", query,
                e);
            return null;
        }
    }

    public ITunesTrackDto lookup(TrackEntity original, String country) {
        return lookup(original.getITunesId(), country);
    }

    public ITunesTrackDto lookup(long id, String country) {
        URI uri = ITunesQueryBuilder.buildLookupUrl(id, ITunesEntityType.SONG, country);
        log.info("ITunes LOOKUP: {}", uri);

        ResponseEntity<SearchResult<ITunesTrackDto>> response = restClient.get()
            .uri(uri)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {
            });

        if (response.getStatusCode().isError() || response.getBody() == null) {
            log.error("Ошибка поиска в ITunes. Статус {}. TrackId: {}", response.getStatusCode(),
                id);
            return null;
        }

        if (response.getBody().getResults().isEmpty()) {
            log.error("Не найдено результатов в ITunes. TrackId: {}", id);
            return null;
        }

        return response.getBody().getResults().stream().findFirst().orElse(null);
    }
}
