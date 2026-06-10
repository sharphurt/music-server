package ru.sharphurt.musicserver.itunes.service;

import static ru.sharphurt.musicserver.util.GlobalConstants.ITUNES_BASE_URL;
import static ru.sharphurt.musicserver.util.GlobalConstants.USER_AGENT;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.ITunesQueryBuilder;
import ru.sharphurt.musicserver.itunes.dto.ITunesSearchResponseDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;

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
        converter.setSupportedMediaTypes(List.of(new MediaType("text", "javascript", StandardCharsets.UTF_8)));
        return converter;
    }

    public Optional<ITunesSearchResponseDto> search(SearchRequestDto searchRequest) {
        try {
            URI uri = ITunesQueryBuilder.buildTrackSearchUrl(searchRequest);

            log.info("ITunes SEARCH: {}", uri);
            ResponseEntity<ITunesSearchResponseDto> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(ITunesSearchResponseDto.class);
            log.warn("iTunes SEARCH Status: {}", response.getStatusCode());

            if (response.getStatusCode().isError() || response.getBody() == null) {
                return Optional.empty();
            }

            return Optional.of(response.getBody());
        } catch (Exception e) {
            log.error("Ошибка при выполнении поиска по iTunes, запрос: {}", searchRequest.query(), e);
            return Optional.empty();
        }
    }

    public Optional<ITunesTrackDto> lookup(TrackDto original, String country) {
        return lookup(original.getITunesId(), country);
    }

    public Optional<ITunesTrackDto> lookup(long id, String country) {
        URI uri = ITunesQueryBuilder.buildLookupUrl(id, ITunesEntityType.SONG, country);
        log.info("ITunes LOOKUP: {}", uri);

        ResponseEntity<ITunesSearchResponseDto> response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(ITunesSearchResponseDto.class);
        log.info("iTunes LOOKUP Response: {}", response.getBody());

        if (response.getStatusCode().isError() || response.getBody() == null) {
            return Optional.empty();
        }

        return response.getBody().results().stream().findFirst();
    }
}
