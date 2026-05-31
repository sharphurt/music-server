package ru.sharphurt.musicserver.search.itunes;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;

import static ru.sharphurt.musicserver.config.GlobalConstants.ITUNES_BASE_URL;

@Component
public class ITunesQueryBuilder {

    private static final String ENTITY_SONG = "song";

    public String buildTrackSearchUrl(SearchRequestDto request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new IllegalArgumentException("Поисковый запрос не может быть пустым");
        }

        long offset = Math.max(0, (request.getPage() - 1) * request.getLimit());

        return UriComponentsBuilder.fromUriString(ITUNES_BASE_URL)
                .queryParam("term", request.getQuery().trim())
                .queryParam("entity", ENTITY_SONG)
                .queryParam("limit", request.getLimit())
                .queryParam("offset", offset)
                .toUriString();
    }
}