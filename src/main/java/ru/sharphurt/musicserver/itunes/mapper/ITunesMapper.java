package ru.sharphurt.musicserver.itunes.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.util.Utils;

@Slf4j
@Component
@RequiredArgsConstructor
public class ITunesMapper {

    @Value("${server-base-url}")
    private String serverBaseUrl;

    public List<TrackEntity> mapAllToTrackDto(List<ITunesTrackDto> trackDtos) {
        return trackDtos.stream().map(this::mapToTrackDto).toList();
    }

    public TrackEntity mapToTrackDto(ITunesTrackDto dto) {
        String previewUrl = Utils.buildProxyUrl(dto.previewUrl(), serverBaseUrl);
        String rawImageUrl = Utils.buildProxyUrl(dto.artworkUrl100()
            .replace("100x100bb.jpg", "600x600bb.jpg"), serverBaseUrl);
        List<String> imageUrls = rawImageUrl != null ? List.of(rawImageUrl) : List.of();

        boolean isExplicit = "explicit".equalsIgnoreCase(dto.trackExplicitness());

        return TrackEntity.builder()
            .iTunesId(dto.trackId())
            .artistId(dto.artistId())
            .albumId(dto.collectionId())

            .title(dto.trackName())
            .artistName(dto.artistName())
            .albumArtistName(dto.artistName())
            .albumName(dto.collectionName())

            .trackNumber(dto.trackNumber())
            .discNumber(dto.discNumber())

            .genres(dto.primaryGenreName() != null ? List.of(dto.primaryGenreName()) : List.of())
            .imageUrls(imageUrls)
            .previewUrl(previewUrl)

            .mbid(null)
            .duration(dto.trackTimeMillis())
            .releaseDate(dto.releaseDate())
            .isExplicit(isExplicit)
            .build();
    }
}