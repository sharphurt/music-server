package ru.sharphurt.musicserver.mediametadata.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sharphurt.musicserver.common.GlobalConstants;

@UtilityClass
public class ProxyUrlUtils {

    public static String buildProxyUrl(String url, String serverBaseUrl) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        return UriComponentsBuilder.fromUriString(serverBaseUrl)
            .path(GlobalConstants.PROXY_ENDPOINT)
            .queryParam("url", url)
            .toUriString();
    }

    public static String buildDownloadUrl(String title, String artist, String album,
        String serverBaseUrl) {
        return UriComponentsBuilder.fromUriString(serverBaseUrl)
            .path(GlobalConstants.DOWNLOAD_ENDPOINT)
            .queryParam("name", title)
            .queryParam("artist", artist)
            .queryParam("album", album)
            .toUriString();
    }
}
