package ru.sharphurt.musicserver.itunes;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sharphurt.musicserver.itunes.service.ITunesEntityType;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;

import java.net.URI;

import static ru.sharphurt.musicserver.util.GlobalConstants.ITUNES_BASE_URL;

@UtilityClass
public class ITunesQueryBuilder {

    private static final String LOOKUP_PATH = "/lookup";

    private static final String SEARCH_PATH = "/search";


    public static URI buildTrackSearchUrl(SearchRequestDto request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Поисковый запрос не может быть пустым");
        }

        long offset = Math.max(0, (request.page() - 1) * request.limit());

        return UriComponentsBuilder.fromUriString(ITUNES_BASE_URL)
                .path(SEARCH_PATH)
                .queryParam("term", request.query().trim())
                .queryParam("entity", ITunesEntityType.SONG.getApiName())
                .queryParam("limit", request.limit())
                .queryParam("offset", offset)
                .encode()
                .build()
                .toUri();
    }

    public static URI buildLookupUrl(long id, ITunesEntityType entity, String country) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ITUNES_BASE_URL)
                .path(LOOKUP_PATH)
                .queryParam("id", id)
                .queryParam("entity", entity.getApiName());

        if (country != null && !country.isEmpty()) {
            builder.queryParam("country", country);
        }

        return builder
                .encode()
                .build()
                .toUri();
    }
}