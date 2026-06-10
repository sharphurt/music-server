package ru.sharphurt.musicserver.itunes.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.util.Utils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITunesMappingService {

    @Value("${server-base-url}")
    private String serverBaseUrl;

    public List<TrackDto> mapToTrackDto(List<ITunesTrackDto> trackDtos) {
        return trackDtos.stream().map(this::mapToTrackDto).toList();
    }

    public TrackDto mapToTrackDto(ITunesTrackDto dto) {
        String previewUrl = Utils.buildProxyUrl(dto.previewUrl(), serverBaseUrl);
        String rawImageUrl = Utils.buildProxyUrl(dto.artworkUrl100()
            .replace("100x100bb.jpg", "600x600bb.jpg"), serverBaseUrl);
        List<String> imageUrls = rawImageUrl != null ? List.of(rawImageUrl) : List.of();

        boolean isExplicit = "explicit".equalsIgnoreCase(dto.trackExplicitness());

        return TrackDto.builder()
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
            .downloadUrl(
                Utils.buildDownloadUrl(dto.trackName(), dto.artistName(), dto.collectionName(),
                    serverBaseUrl))
            .previewUrl(previewUrl)

            .mbid(null)
            .playcounts(0L)
            .duration(dto.trackTimeMillis())
            .releaseDate(dto.releaseDate())
            .isExplicit(isExplicit)
            .build();
    }
}