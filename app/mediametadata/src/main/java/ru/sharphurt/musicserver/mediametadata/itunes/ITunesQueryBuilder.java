package ru.sharphurt.musicserver.mediametadata.itunes;

import java.net.URI;
import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sharphurt.musicserver.common.GlobalConstants;

@UtilityClass
public class ITunesQueryBuilder {

    private static final String LOOKUP_PATH = "/lookup";
    private static final String SEARCH_PATH = "/search";

    public static URI buildSearchUrl(String query, ITunesEntityType entity, long limit, long page) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Поисковый запрос не может быть пустым");
        }

        long offset = Math.max(0, (page - 1) * limit);

        return UriComponentsBuilder.fromUriString(GlobalConstants.ITUNES_BASE_URL)
            .path(SEARCH_PATH)
            .queryParam("term", query.trim())
            .queryParam("entity", entity.getApiName())
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .encode()
            .build()
            .toUri();
    }

    public static URI buildLookupUrl(long id, ITunesEntityType entity, String country) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(GlobalConstants.ITUNES_BASE_URL)
            .path(LOOKUP_PATH)
            .queryParam("id", id)
            .queryParam("entity", entity.getApiName());

        if (country != null && !country.isBlank()) {
            builder.queryParam("country", country);
        }

        return builder.encode().build().toUri();
    }
}