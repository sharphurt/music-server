package ru.sharphurt.musicserver.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TrackDto extends BaseEntityDto {

    private String artistName;

    private String albumName;

    private long playcounts;

    private long duration;

    private LocalDateTime releaseDate;

    @Builder
    public TrackDto(Long iTunesId,
                    String title,
                    List<String> genres,
                    String imageUrl,
                    String downloadUrl,
                    String mbid,
                    String artistName,
                    String albumName,
                    long duration,
                    long playcount,
                    LocalDateTime releaseDate) {
        super(iTunesId, title, genres, new ArrayList<>(List.of(imageUrl)), downloadUrl, mbid);
        this.artistName = artistName;
        this.albumName = albumName;
        this.playcounts = playcount;
        this.duration = duration;
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "Track: [%s] by [%s]".formatted(getTitle(), getArtistName());
    }
}
