package ru.sharphurt.musicserver.search.musicbrainz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.search.dto.RawSearchResultDto;
import ru.sharphurt.musicserver.search.dto.TrackDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicBrainzMapper {

    private final ObjectMapper objectMapper;

    public RawSearchResultDto<TrackDto> mapToTrackSearchDtos(String responseBody, String query, long limit) {
        List<TrackDto> dtos = new ArrayList<>();
        long totalResults = 0;
        long offset = 0;

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode recordings = root.path("recordings");
            totalResults = Long.parseLong(root.path("count").asText("0"));
            offset = Long.parseLong(root.path("offset").asText("0"));

            if (recordings.isArray()) {
                for (JsonNode recording : recordings) {
                    dtos.add(parseRecording(recording));
                }
            }
        } catch (Exception e) {
            log.error("Ошибка парсинга JSON от MusicBrainz", e);
        }

        return RawSearchResultDto.withContent(dtos, totalResults, query, limit, offset / limit + 1);
    }

    private TrackDto parseRecording(JsonNode recording) {
        String mbid = recording.path("id").asText(null);
        String title = recording.path("title").asText("Unknown Title");

        String authorName = "Unknown Artist";
        JsonNode artistCredit = recording.path("artist-credit");
        if (artistCredit.isArray() && !artistCredit.isEmpty()) {
            authorName = artistCredit.get(0).path("name").asText("Unknown Artist");
        }

        String albumName = "Unknown Album";
        String imageUrl = null;
        JsonNode releases = recording.path("releases");

        if (releases.isArray() && !releases.isEmpty()) {
            JsonNode firstRelease = releases.get(0);
            albumName = firstRelease.path("title").asText("Unknown Album");

            String releaseId = firstRelease.path("id").asText(null);
            if (releaseId != null) {
                imageUrl = String.format("https://coverartarchive.org/release/%s/front", releaseId);
            }
        }

        List<String> genres = new ArrayList<>();
        JsonNode tags = recording.path("tags");
        if (tags.isArray()) {
            for (JsonNode tag : tags) {
                genres.add(tag.path("name").asText());
            }
        }

        return TrackDto
                .builder()
                .title(title)
                .genres(genres)
                .imageUrl(imageUrl)
                .mbid(mbid)
                .artistName(authorName)
                .albumName(albumName)
                .playcount(0L)
                .build();
    }
}