package ru.sharphurt.musicserver.search.musicbrainz;

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

import static ru.sharphurt.musicserver.util.GlobalConstants.MUSICBRAINZ_BASE_URL;
import static ru.sharphurt.musicserver.util.GlobalConstants.USER_AGENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicBrainzSearchService implements SearchProvider {

    private final MusicBrainzQueryBuilder queryBuilder;

    private final MusicBrainzMapper mapper;
    private final RestClient restClient = RestClient.builder()
            .baseUrl(MUSICBRAINZ_BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public RawSearchResultDto<TrackDto> searchTracks(SearchRequestDto request) {
        if (request.getQuery().trim().isEmpty()) {
            return RawSearchResultDto.empty(request);
        }

        long offset = (request.getPage() - 1) * request.getLimit();
        String luceneQuery = queryBuilder.buildLuceneQuery(request.getQuery());
        log.info("Сгенерированный Lucene запрос: {}", luceneQuery);

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ws/2/recording")
                            .queryParam("query", luceneQuery)
                            .queryParam("limit", request.getLimit())
                            .queryParam("offset", offset)
                            .queryParam("fmt", "json")
                            .build())
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.mapToTrackSearchDtos(response.getBody(), request.getQuery(), request.getLimit());
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе к MusicBrainz API", e);
        }

        return RawSearchResultDto.empty(request);
    }
}