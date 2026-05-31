package ru.sharphurt.musicserver.search.itunes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.search.SearchProvider;
import ru.sharphurt.musicserver.search.dto.RawSearchResultDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.TrackDto;

import java.net.URI;

import static ru.sharphurt.musicserver.config.GlobalConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesSearchService implements SearchProvider {

    private final ITunesQueryBuilder iTunesQueryBuilder;
    private final ITunesMapper mapper;
    private final RestClient restClient = RestClient.builder()
            .baseUrl(ITUNES_BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Override
    public RawSearchResultDto<TrackDto> searchTracks(SearchRequestDto request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return RawSearchResultDto.empty(request);
        }

        String iTunesUrl = iTunesQueryBuilder.buildTrackSearchUrl(request);
        log.info("Выполнение запроса к iTunes API: {}", iTunesUrl);

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(URI.create(iTunesUrl))
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.mapToTrackSearchDtos(response.getBody(), request);
            }

            log.warn("iTunes API вернул статус: {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса к iTunes API для запроса: {}", request.getQuery(), e);
        }

        return RawSearchResultDto.empty(request);
    }
}