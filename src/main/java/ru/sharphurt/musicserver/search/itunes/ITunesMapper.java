package ru.sharphurt.musicserver.search.itunes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.search.dto.RawSearchResultDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.TrackDto;
import ru.sharphurt.musicserver.util.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ITunesMapper {

    private final ObjectMapper objectMapper;

    @Value("${server-base-url}")
    private String serverBaseUrl;

    public RawSearchResultDto<TrackDto> mapToTrackSearchDtos(String jsonResponse, SearchRequestDto request) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode resultsNode = root.path("results");
            long resultCount = root.path("resultCount").asLong(0);

            List<TrackDto> tracks = new ArrayList<>();
            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    tracks.add(mapToTrackDto(node));
                }
            }

            long startIndex = (request.getPage() - 1) * request.getLimit();

            return RawSearchResultDto.<TrackDto>builder()
                    .query(request.getQuery())
                    .page(request.getPage())
                    .pageSize(request.getLimit())
                    .entities(tracks)
                    .totalCount(startIndex + resultCount)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при десериализации JSON от iTunes API", e);
            return RawSearchResultDto.empty(request);
        }
    }

    private TrackDto mapToTrackDto(JsonNode node) {
        String title = node.path("trackName").asText("");
        String artist = node.path("artistName").asText("");
        String album = node.path("collectionName").asText("");
        LocalDateTime releaseDate = parseDateTimeOrNull(node.path("releaseDate").asText(""));
        long durationMs = node.path("trackTimeMillis").asLong(0);
        long iTunesId = node.path("trackId").asLong(0);
        String genre = node.path("primaryGenreName").asText(null);
        List<String> genres = genre != null ? List.of(genre) : List.of();

        String previewUrl = Utils.buildProxyUrl(node.path("previewUrl").asText(), serverBaseUrl);
        String rawImageUrl = Utils.buildProxyUrl(node.path("artworkUrl100")
                .asText("")
                .replace("100x100bb.jpg", "600x600bb.jpg"), serverBaseUrl);
        List<String> imageUrls = rawImageUrl != null ? List.of(rawImageUrl) : List.of();

        return TrackDto
                .builder()
                .iTunesId(iTunesId)
                .title(title)
                .genres(genres)
                .imageUrls(imageUrls)
                .artistName(artist)
                .albumName(album)
                .playcounts(0L)
                .duration(durationMs)
                .previewUrl(previewUrl)
                .releaseDate(releaseDate)
                .build();
    }

    private LocalDateTime parseDateTimeOrNull(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

}